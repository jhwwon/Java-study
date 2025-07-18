package banksystem.manager;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SchedulerManager {
    private Timer timer;
    private Connection conn;
    private AdminManager adminManager;
    private boolean isRunning = false;
    
    // 실행 시간 설정 (오후 2시)
    private static final int TARGET_HOUR = 14;
    private static final int TARGET_MINUTE = 0;
    private static final int TARGET_SECOND = 0;
    
    public SchedulerManager(Connection conn, AdminManager adminManager) {
        this.conn = conn;
        this.adminManager = adminManager;
        // 데몬 스레드 이용 (백그라운드에서 실행되는 보조 스레드)
        this.timer = new Timer("월말이자지급스케줄러", true); 
    }
    
    // 스케줄러 시작
    public void start() {
        if (isRunning) {
            System.out.println("스케줄러가 이미 실행 중입니다.");
            return;
        }
        
        System.out.println("월말 이자 지급 스케줄러를 시작합니다...");
        
        // 1단계: 놓친 지급이 있는지 체크
        checkMissedPayments();
        
        // 2단계: 다음 실행 스케줄 등록
        scheduleNextPayment();
        
        isRunning = true;
        System.out.println("✅ 스케줄러가 성공적으로 시작되었습니다!");
    }
    
    // 놓친 지급 체크 및 실행
    private void checkMissedPayments() {
        try {
            Date lastPaymentDate = getLastInterestPaymentDate();
            Date lastMonthEndDate = getLastMonthEnd();
            
            // 지난달 말일 이후에 이자 지급이 없었다면 놓친 것
            if (lastPaymentDate == null || lastPaymentDate.before(lastMonthEndDate)) {
                System.out.println("놓친 이자 지급을 발견했습니다!");
                System.out.println("마지막 지급일: " + (lastPaymentDate != null ? lastPaymentDate : "없음"));
                System.out.println("예상 지급일: " + lastMonthEndDate);
                System.out.println("지금 즉시 이자를 지급합니다...");
                
                // 즉시 이자 지급 실행
                executeInterestPayment("SYSTEM_AUTO");
            } else {
                System.out.println("✅ 놓친 이자 지급이 없습니다.");
            }
        } catch (Exception e) {
            System.out.println("놓친 지급 체크 중 오류: " + e.getMessage());
        }
    }
    
    // 다음 실행 스케줄 등록
    private void scheduleNextPayment() {
        Date nextExecutionDate = calculateNextExecutionDate();
        
        // TimerTask 생성
        TimerTask paymentTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    System.out.println("월말 이자 지급 시간입니다! (" + new Date() + ")");
                    executeInterestPayment("SCHEDULER_AUTO");
                    
                    // 다음 달 스케줄 재등록
                    scheduleNextPayment();
                    
                } catch (Exception e) {
                    System.out.println("자동 이자 지급 실행 중 오류: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        
        // 스케줄 등록
        timer.schedule(paymentTask, nextExecutionDate);
    }
    
    // 다음 실행일 계산 (이번 달 마지막 날 오후 2시)
    private Date calculateNextExecutionDate() {
        Calendar cal = Calendar.getInstance();
        
        // 이번 달 마지막 날로 설정
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, TARGET_HOUR);
        cal.set(Calendar.MINUTE, TARGET_MINUTE);
        cal.set(Calendar.SECOND, TARGET_SECOND);
        cal.set(Calendar.MILLISECOND, 0);
        
        Date thisMonthEnd = cal.getTime();
        Date now = new Date();
        
        // 만약 이번 달 마지막 날 오후 2시가 이미 지났다면 다음 달로
        if (thisMonthEnd.before(now) || thisMonthEnd.equals(now)) {
            cal.add(Calendar.MONTH, 1);
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            cal.set(Calendar.HOUR_OF_DAY, TARGET_HOUR);
            cal.set(Calendar.MINUTE, TARGET_MINUTE);
            cal.set(Calendar.SECOND, TARGET_SECOND);
            cal.set(Calendar.MILLISECOND, 0);
        }
        
        return cal.getTime();
    }
    
    // 지난달 마지막 날 계산
    private Date getLastMonthEnd() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1); // 지난달로
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, TARGET_HOUR);
        cal.set(Calendar.MINUTE, TARGET_MINUTE);
        cal.set(Calendar.SECOND, TARGET_SECOND);
        cal.set(Calendar.MILLISECOND, 0);
        
        return cal.getTime();
    }
    
    // 마지막 이자 지급일 조회
    private Date getLastInterestPaymentDate() {
        String sql = "SELECT MAX(payment_date) as last_payment FROM interest_payments";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getTimestamp("last_payment");
                }
            }
        } catch (SQLException e) {
            System.out.println("마지막 이자 지급일 조회 오류: " + e.getMessage());
        }
        
        return null;
    }
    
    // 이자 지급 실행
    private void executeInterestPayment(String executorId) {
        if (adminManager != null) {
            System.out.println("이자 일괄 지급을 실행합니다... (실행자: " + executorId + ")");
            adminManager.executeInterestPayment(executorId);
            System.out.println("이자 일괄 지급이 완료되었습니다!");
        } else {
            System.out.println("AdminManager가 설정되지 않아 이자 지급을 실행할 수 없습니다.");
        }
    }
    
    // 스케줄러 중지
    public void stop() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            isRunning = false;
            System.out.println("월말 이자 지급 스케줄러가 중지되었습니다.");
        }
    }
    
    // 다음 실행일 조회 (관리자용)
    public String getNextExecutionInfo() {
        Date nextDate = calculateNextExecutionDate();
        long remainingTime = nextDate.getTime() - System.currentTimeMillis();
        long remainingDays = remainingTime / (1000 * 60 * 60 * 24);
        long remainingHours = (remainingTime % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        
        return String.format("다음 실행: %s (남은 시간: %d일 %d시간)", 
                           nextDate.toString(), remainingDays, remainingHours);
    }
    
    // 스케줄러 상태 확인
    public boolean isRunning() {
        return isRunning;
    }
}