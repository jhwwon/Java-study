package gui;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.SimpleDateFormat;

// 데이터 클래스
class User {
    String id, pw;
    User(String id, String pw) { this.id=id; this.pw=pw; }
}

class Income {
    int amount;
    String desc;
    String date;
    Income(int amount, String desc, String date) { 
        this.amount=amount; 
        this.desc=desc; 
        this.date=date;
    }
}

class Expense {
    int amount;
    String desc;
    String date;
    Expense(int amount, String desc, String date) { 
        this.amount=amount; 
        this.desc=desc; 
        this.date=date;
    }
}

public class SimpleFinanceGUI extends JFrame {
    // 귀여운 파스텔 색상 테마 🌈
    private static final Color CUTE_PINK = new Color(255, 182, 193);        // 연한 핑크
    private static final Color CUTE_BLUE = new Color(173, 216, 230);        // 연한 하늘색
    private static final Color CUTE_GREEN = new Color(144, 238, 144);       // 연한 연두색
    private static final Color CUTE_PURPLE = new Color(221, 160, 221);      // 연한 보라색
    private static final Color CUTE_YELLOW = new Color(255, 255, 224);      // 연한 노랑색
    private static final Color CUTE_ORANGE = new Color(255, 218, 185);      // 연한 주황색
    private static final Color BACKGROUND_COLOR = new Color(255, 250, 240); // 크림색
    private static final Color CARD_COLOR = Color.WHITE;                    // 순백색
    private static final Color TEXT_COLOR = new Color(105, 105, 105);       // 부드러운 회색
    
    java.util.List<User> users = new ArrayList<User>();
    java.util.Map<String, java.util.List<Income>> incomes = new HashMap<String, java.util.List<Income>>();
    java.util.Map<String, java.util.List<Expense>> expenses = new HashMap<String, java.util.List<Expense>>();
    String loginId = null;

    CardLayout cards = new CardLayout();
    JPanel mainPanel = new JPanel(cards);

    public SimpleFinanceGUI() {
        setTitle("🌸 귀여운 가계부 🌸");
        setSize(650, 550);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // 전체 배경색 설정
        getContentPane().setBackground(BACKGROUND_COLOR);
        mainPanel.setBackground(BACKGROUND_COLOR);

        mainPanel.add(createLoginPanel(), "login");
        mainPanel.add(createRegisterPanel(), "register");
        mainPanel.add(createHomePanel(), "home");
        mainPanel.add(createIncomePanel(), "income");
        mainPanel.add(createExpensePanel(), "expense");
        mainPanel.add(createSummaryPanel(), "summary");
        add(mainPanel);

        showPanel("login");
    }

    void showPanel(String name) {
        if(name.equals("home")) setTitle("🌸 귀여운 가계부 - " + loginId + "님 🌸");
        else setTitle("🌸 귀여운 가계부 🌸");
        cards.show(mainPanel, name);
    }

    // 현재 날짜를 yyyy-MM-dd 형식으로 반환
    String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date());
    }
    
    // 귀여운 버튼 생성 메소드 ✨
    JButton createCuteButton(String text, Color bgColor, Color textColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(textColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        button.setPreferredSize(new Dimension(140, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 둥근 모서리 효과
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bgColor.darker(), 2),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        
        // 귀여운 호버 효과 💫
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.brighter());
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(bgColor, 3),
                    BorderFactory.createEmptyBorder(7, 14, 7, 14)
                ));
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(bgColor.darker(), 2),
                    BorderFactory.createEmptyBorder(8, 15, 8, 15)
                ));
            }
        });
        
        return button;
    }
    
    // 귀여운 카드 패널 생성 메소드 🎀
    JPanel createCuteCard() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CUTE_PINK, 3),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        return panel;
    }
    
    // 귀여운 텍스트 필드 생성 메소드 🌺
    JTextField createCuteTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CUTE_BLUE, 2),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        field.setBackground(new Color(255, 255, 255));
        return field;
    }
    
    // 귀여운 패스워드 필드 생성 메소드 🔒
    JPasswordField createCutePasswordField(int columns) {
        JPasswordField field = new JPasswordField(columns);
        field.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CUTE_BLUE, 2),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        field.setBackground(new Color(255, 255, 255));
        return field;
    }

    JPanel createLoginPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        // 귀여운 제목 패널 🌸
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(BACKGROUND_COLOR);
        JLabel titleLabel = new JLabel("🌸✨ 가계부 ✨🌸", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 32));
        titleLabel.setForeground(CUTE_PINK.darker());
        titleLabel.setBorder(BorderFactory.createEmptyBorder(30, 0, 10, 0));
        
        JLabel subLabel = new JLabel("돈 관리는 소중하게!", SwingConstants.CENTER);
        subLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        subLabel.setForeground(TEXT_COLOR);
        subLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.add(titleLabel);
        titlePanel.add(subLabel);
        
        // 로그인 카드 패널 💝
        JPanel cardPanel = createCuteCard();
        cardPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        
        final JTextField idField = createCuteTextField(18);
        final JPasswordField pwField = createCutePasswordField(18);
        JButton loginBtn = createCuteButton("🌟 로그인하기 🌟", CUTE_PINK, Color.WHITE);
        JButton regBtn = createCuteButton("🎀 회원가입하기 🎀", CUTE_BLUE, Color.WHITE);

        // 귀여운 라벨들 🎨
        JLabel idLabel = new JLabel("🐱 아이디");
        JLabel pwLabel = new JLabel("🔐 비밀번호");
        idLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        pwLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        idLabel.setForeground(TEXT_COLOR);
        pwLabel.setForeground(TEXT_COLOR);

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        cardPanel.add(idLabel, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        cardPanel.add(idField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.WEST;
        cardPanel.add(pwLabel, gbc);
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        cardPanel.add(pwField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 12, 8, 12);
        cardPanel.add(loginBtn, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.insets = new Insets(8, 12, 12, 12);
        cardPanel.add(regBtn, gbc);

        loginBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String id = idField.getText().trim();
                String pw = new String(pwField.getPassword());
                boolean ok = false;
                for(User u: users) {
                    if(u.id.equals(id) && u.pw.equals(pw)) { 
                        ok=true; 
                        break; 
                    }
                }
                if(ok) {
                    loginId = id;
                    idField.setText("");
                    pwField.setText("");
                    showPanel("home");
                } else {
                    JOptionPane.showMessageDialog(SimpleFinanceGUI.this, 
                        "앗! 아이디나 비밀번호가 틀렸어요 😅💦", 
                        "로그인 실패", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        regBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showPanel("register");
            }
        });
        
        pwField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loginBtn.doClick();
            }
        });
        
        // 중앙에 카드 패널 배치
        JPanel centerPanel = new JPanel(new FlowLayout());
        centerPanel.setBackground(BACKGROUND_COLOR);
        centerPanel.add(cardPanel);
        
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        return mainPanel;
    }

    JPanel createRegisterPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        // 제목
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(BACKGROUND_COLOR);
        JLabel titleLabel = new JLabel("🎀✨ 회원가입 ✨🎀", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 26));
        titleLabel.setForeground(CUTE_BLUE.darker());
        titleLabel.setBorder(BorderFactory.createEmptyBorder(25, 0, 15, 0));
        
        JLabel subLabel = new JLabel("함께 가계부를 시작해요! 🥰", SwingConstants.CENTER);
        subLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
        subLabel.setForeground(TEXT_COLOR);
        
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.add(titleLabel);
        titlePanel.add(subLabel);
        
        JPanel cardPanel = createCuteCard();
        cardPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 12, 10, 12);
        
        final JTextField idField = createCuteTextField(18);
        final JPasswordField pwField = createCutePasswordField(18);
        final JPasswordField confirmField = createCutePasswordField(18);
        JButton regBtn = createCuteButton("🌈 가입완료! 🌈", CUTE_GREEN, Color.WHITE);
        JButton backBtn = createCuteButton("🔙 돌아가기", CUTE_PURPLE, Color.WHITE);

        JLabel idLabel = new JLabel("🐰 아이디");
        JLabel pwLabel = new JLabel("🔒 비밀번호");
        JLabel confirmLabel = new JLabel("🔑 비밀번호 확인");
        
        Font labelFont = new Font("맑은 고딕", Font.BOLD, 14);
        idLabel.setFont(labelFont); pwLabel.setFont(labelFont); confirmLabel.setFont(labelFont);
        idLabel.setForeground(TEXT_COLOR); pwLabel.setForeground(TEXT_COLOR); confirmLabel.setForeground(TEXT_COLOR);

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        cardPanel.add(idLabel, gbc);
        gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        cardPanel.add(idField, gbc);
        
        gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.WEST;
        cardPanel.add(pwLabel, gbc);
        gbc.gridy = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        cardPanel.add(pwField, gbc);
        
        gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.WEST;
        cardPanel.add(confirmLabel, gbc);
        gbc.gridy = 5; gbc.fill = GridBagConstraints.HORIZONTAL;
        cardPanel.add(confirmField, gbc);
        
        gbc.gridy = 6; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 12, 8, 12);
        cardPanel.add(regBtn, gbc);
        
        gbc.gridy = 7; gbc.insets = new Insets(8, 12, 12, 12);
        cardPanel.add(backBtn, gbc);

        regBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String id = idField.getText().trim();
                String pw = new String(pwField.getPassword());
                String confirm = new String(confirmField.getPassword());
                
                if(id.isEmpty() || pw.isEmpty()) {
                    JOptionPane.showMessageDialog(SimpleFinanceGUI.this, 
                        "빈칸이 있어요! 모두 채워주세요 🥺💕", "입력 필요", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                if(!pw.equals(confirm)) {
                    JOptionPane.showMessageDialog(SimpleFinanceGUI.this, 
                        "비밀번호가 달라요! 다시 확인해주세요 😊💦", "확인 필요", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                for(User u: users) {
                    if(u.id.equals(id)) {
                        JOptionPane.showMessageDialog(SimpleFinanceGUI.this, 
                            "이미 있는 아이디예요! 다른 걸로 해주세요 😅", "아이디 중복", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                users.add(new User(id, pw));
                JOptionPane.showMessageDialog(SimpleFinanceGUI.this, 
                    "환영해요! 가입이 완료되었어요! 🎉✨", "가입 성공", JOptionPane.INFORMATION_MESSAGE);
                showPanel("login");
            }
        });
        
        backBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showPanel("login");
            }
        });
        
        JPanel centerPanel = new JPanel(new FlowLayout());
        centerPanel.setBackground(BACKGROUND_COLOR);
        centerPanel.add(cardPanel);
        
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        return mainPanel;
    }

    JPanel createHomePanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        // 상단 환영 메시지 🌈
        final JPanel welcomePanel = new JPanel();
        welcomePanel.setBackground(CUTE_PINK);
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        final JLabel welcomeLabel = new JLabel("", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        welcomeLabel.setForeground(Color.WHITE);
        welcomePanel.add(welcomeLabel);
        
        // 귀여운 메뉴 버튼들 🎀
        JPanel menuPanel = new JPanel(new GridLayout(2, 2, 25, 25));
        menuPanel.setBackground(BACKGROUND_COLOR);
        menuPanel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));
        
        JButton incomeBtn = createCuteMenuButton("🌟💰🌟", "수입 관리", "돈돈이가 들어왔어요!", CUTE_GREEN);
        JButton expenseBtn = createCuteMenuButton("🛍️💸🛍️", "지출 관리", "어디에 썼는지 기록해요!", CUTE_ORANGE);
        JButton summaryBtn = createCuteMenuButton("📊✨📊", "요약 보기", "지금까지 얼마나 모았을까요?", CUTE_PURPLE);
        JButton logoutBtn = createCuteMenuButton("🚪👋🚪", "로그아웃", "안전하게 나가기", CUTE_BLUE);
        
        menuPanel.add(incomeBtn);
        menuPanel.add(expenseBtn);
        menuPanel.add(summaryBtn);
        menuPanel.add(logoutBtn);

        incomeBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { showPanel("income"); }
        });
        
        expenseBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { showPanel("expense"); }
        });
        
        summaryBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { showPanel("summary"); }
        });
        
        logoutBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int result = JOptionPane.showConfirmDialog(SimpleFinanceGUI.this, 
                    "정말 로그아웃할까요? 🥺👋", "로그아웃", 
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if(result == JOptionPane.YES_OPTION) {
                    loginId = null;
                    showPanel("login");
                }
            }
        });
        
        mainPanel.addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent e) {
                welcomeLabel.setText("🌸 " + loginId + "님, 안녕하세요! 🌸");
            }
        });
        
        mainPanel.add(welcomePanel, BorderLayout.NORTH);
        mainPanel.add(menuPanel, BorderLayout.CENTER);
        
        return mainPanel;
    }
    
    JButton createCuteMenuButton(String icon, String title, String description, Color color) {
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBackground(color);
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker(), 3),
            BorderFactory.createEmptyBorder(20, 15, 20, 15)
        ));
        buttonPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        descLabel.setForeground(Color.WHITE);
        descLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setBackground(color);
        textPanel.add(titleLabel);
        textPanel.add(descLabel);
        
        buttonPanel.add(iconLabel, BorderLayout.NORTH);
        buttonPanel.add(textPanel, BorderLayout.CENTER);
        
        JButton button = new JButton();
        button.setLayout(new BorderLayout());
        button.add(buttonPanel);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                buttonPanel.setBackground(color.brighter());
                textPanel.setBackground(color.brighter());
                buttonPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(color, 4),
                    BorderFactory.createEmptyBorder(19, 14, 19, 14)
                ));
            }
            public void mouseExited(MouseEvent e) {
                buttonPanel.setBackground(color);
                textPanel.setBackground(color);
                buttonPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(color.darker(), 3),
                    BorderFactory.createEmptyBorder(20, 15, 20, 15)
                ));
            }
        });
        
        return button;
    }

    JPanel createIncomePanel() {
        return createFinancePanel("🌟💰 수입 관리 💰🌟", "소중한 돈돈이 들어온 날!", CUTE_GREEN, true);
    }

    JPanel createExpensePanel() {
        return createFinancePanel("🛍️💸 지출 관리 💸🛍️", "어디에 돈돈이를 썼을까요?", CUTE_ORANGE, false);
    }
    
    JPanel createFinancePanel(String title, String subtitle, Color themeColor, final boolean isIncome) {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        // 제목 패널 🎨
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(themeColor);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subLabel = new JLabel(subtitle);
        subLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        subLabel.setForeground(Color.WHITE);
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        titlePanel.add(subLabel);
        
        // 버튼 패널 🎀
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JButton addBtn = createCuteButton(isIncome ? "💰 추가하기" : "💸 추가하기", themeColor, Color.WHITE);
        JButton deleteBtn = createCuteButton("🗑️ 삭제하기", CUTE_PINK, Color.WHITE);
        JButton backBtn = createCuteButton("🏠 홈으로", CUTE_PURPLE, Color.WHITE);
        
        buttonPanel.add(addBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(backBtn);
        
        // 내용 표시 영역 📝
        final JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        area.setBackground(CARD_COLOR);
        area.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        area.setLineWrap(true);
        
        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(15, 15, 15, 15),
            BorderFactory.createLineBorder(themeColor, 2)
        ));
        scrollPane.setBackground(BACKGROUND_COLOR);

        addBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showCuteAddDialog(isIncome, area);
            }
        });
        
        deleteBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showCuteDeleteDialog(isIncome, area);
            }
        });
        
        backBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showPanel("home");
            }
        });

        mainPanel.addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent e) {
                if(isIncome) updateIncomeArea(area);
                else updateExpenseArea(area);
            }
        });
        
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        mainPanel.add(scrollPane, BorderLayout.SOUTH);
        
        return mainPanel;
    }
    
    void showCuteAddDialog(boolean isIncome, JTextArea area) {
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 15, 15));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JTextField amtField = createCuteTextField(12);
        JTextField descField = createCuteTextField(12);
        JTextField dateField = createCuteTextField(12);
        dateField.setText(getCurrentDate());
        
        // 귀여운 라벨들 🎀
        JLabel amtLabel = new JLabel(isIncome ? "💰 얼마나 들어왔나요?" : "💸 얼마나 썼나요?");
        JLabel descLabel = new JLabel("📝 무엇인가요?");
        JLabel dateLabel = new JLabel("📅 언제인가요?");
        
        Font labelFont = new Font("맑은 고딕", Font.BOLD, 13);
        amtLabel.setFont(labelFont);
        descLabel.setFont(labelFont);
        dateLabel.setFont(labelFont);
        amtLabel.setForeground(TEXT_COLOR);
        descLabel.setForeground(TEXT_COLOR);
        dateLabel.setForeground(TEXT_COLOR);
        
        inputPanel.add(amtLabel);
        inputPanel.add(amtField);
        inputPanel.add(descLabel);
        inputPanel.add(descField);
        inputPanel.add(dateLabel);
        inputPanel.add(dateField);
        
        int result = JOptionPane.showConfirmDialog(this, inputPanel, 
            isIncome ? "🌟 수입 추가하기 🌟" : "🛍️ 지출 추가하기 🛍️", JOptionPane.OK_CANCEL_OPTION);
        
        if(result == JOptionPane.OK_OPTION) {
            try {
                int amt = Integer.parseInt(amtField.getText().trim());
                String desc = descField.getText().trim();
                String date = dateField.getText().trim();
                
                if(desc.isEmpty()) desc = isIncome ? "용돈" : "지출";
                
                if(isIncome) {
                    if(!incomes.containsKey(loginId)) incomes.put(loginId, new ArrayList<Income>());
                    incomes.get(loginId).add(new Income(amt, desc, date));
                    updateIncomeArea(area);
                } else {
                    if(!expenses.containsKey(loginId)) expenses.put(loginId, new ArrayList<Expense>());
                    expenses.get(loginId).add(new Expense(amt, desc, date));
                    updateExpenseArea(area);
                }
                
                JOptionPane.showMessageDialog(this, 
                    (isIncome ? "💰 수입이" : "💸 지출이") + " 추가되었어요! 🎉✨", 
                    "완료!", JOptionPane.INFORMATION_MESSAGE);
            } catch(Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "앗! 숫자만 입력해주세요~ 😅💦", 
                    "입력 오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    void showCuteDeleteDialog(boolean isIncome, JTextArea area) {
        java.util.List<?> list = isIncome ? incomes.get(loginId) : expenses.get(loginId);
        if(list == null || list.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "삭제할 " + (isIncome ? "수입" : "지출") + "이 없어요! 🤔💭", 
                "없어요~", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String[] options = new String[list.size()];
        for(int i = 0; i < list.size(); i++) {
            if(isIncome) {
                Income income = (Income)list.get(i);
                options[i] = income.date + " - " + income.amount + "원 (" + income.desc + ")";
            } else {
                Expense expense = (Expense)list.get(i);
                options[i] = expense.date + " - " + expense.amount + "원 (" + expense.desc + ")";
            }
        }
        
        String selected = (String) JOptionPane.showInputDialog(this,
            "어떤 걸 삭제할까요? 🤗", (isIncome ? "💰 수입" : "💸 지출") + " 삭제하기",
            JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        
        if(selected != null) {
            for(int i = 0; i < options.length; i++) {
                if(options[i].equals(selected)) {
                    list.remove(i);
                    if(isIncome) updateIncomeArea(area);
                    else updateExpenseArea(area);
                    JOptionPane.showMessageDialog(this, 
                        (isIncome ? "💰 수입이" : "💸 지출이") + " 삭제되었어요! ✨", 
                        "삭제 완료!", JOptionPane.INFORMATION_MESSAGE);
                    break;
                }
            }
        }
    }
    
    void updateIncomeArea(JTextArea area) {
        area.setText("");
        java.util.List<Income> list = incomes.get(loginId);
        if(list == null) list = new ArrayList<Income>();
        
        int sum = 0;
        
        area.append("🌟✨ === 소중한 수입 내역 === ✨🌟\n\n");
        if(list.isEmpty()) {
            area.append("🥺 아직 수입이 없어요...\n");
            area.append("💪 용돈이나 알바비를 기록해보세요!\n");
        } else {
            for(Income i : list) {
                area.append("📅 " + i.date + " | 💰 " + i.amount + "원 | 🏷️ " + i.desc + "\n");
                sum += i.amount;
            }
        }
        area.append("\n🎉 총 수입: " + sum + "원 💖");
        if(sum > 0) {
            area.append("\n\n🌟 와우! 열심히 모으고 계시네요! 👏✨");
        }
    }

    void updateExpenseArea(JTextArea area) {
        area.setText("");
        java.util.List<Expense> list = expenses.get(loginId);
        if(list == null) list = new ArrayList<Expense>();
        
        int sum = 0;
        
        area.append("🛍️✨ === 지출 내역 === ✨🛍️\n\n");
        if(list.isEmpty()) {
            area.append("😊 아직 지출이 없어요!\n");
            area.append("💝 필요한 것들을 기록해보세요~\n");
        } else {
            for(Expense i : list) {
                area.append("📅 " + i.date + " | 💸 " + i.amount + "원 | 🏷️ " + i.desc + "\n");
                sum += i.amount;
            }
        }
        area.append("\n💸 총 지출: " + sum + "원");
        if(sum > 0) {
            area.append("\n\n💡 절약할 수 있는 부분이 있는지 살펴보세요! 🤗");
        }
    }

    JPanel createSummaryPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        // 제목 패널 🌈
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(CUTE_PURPLE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        
        JLabel titleLabel = new JLabel("📊✨ 가계부 요약 ✨📊");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subLabel = new JLabel("지금까지 얼마나 모았을까요? 🤔💭");
        subLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        subLabel.setForeground(Color.WHITE);
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        titlePanel.add(subLabel);
        
        // 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JButton refreshBtn = createCuteButton("🔄 새로고침", CUTE_PURPLE, Color.WHITE);
        JButton backBtn = createCuteButton("🏠 홈으로", CUTE_BLUE, Color.WHITE);
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(backBtn);
        
        // 요약 표시 영역
        final JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        area.setBackground(CARD_COLOR);
        area.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        area.setLineWrap(true);
        
        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(15, 15, 15, 15),
            BorderFactory.createLineBorder(CUTE_PURPLE, 2)
        ));
        scrollPane.setBackground(BACKGROUND_COLOR);

        refreshBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateSummaryArea(area);
            }
        });
        
        backBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showPanel("home");
            }
        });

        mainPanel.addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent e) {
                updateSummaryArea(area);
            }
        });
        
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        mainPanel.add(scrollPane, BorderLayout.SOUTH);
        
        return mainPanel;
    }
    
    void updateSummaryArea(JTextArea area) {
        area.setText("");
        
        java.util.List<Income> incomeList = incomes.get(loginId);
        java.util.List<Expense> expenseList = expenses.get(loginId);
        
        if(incomeList == null) incomeList = new ArrayList<Income>();
        if(expenseList == null) expenseList = new ArrayList<Expense>();
        
        int totalIncome = 0;
        int totalExpense = 0;
        
        for(Income i : incomeList) {
            totalIncome += i.amount;
        }
        
        for(Expense e : expenseList) {
            totalExpense += e.amount;
        }
        
        int balance = totalIncome - totalExpense;
        
        area.append("🌸✨ === " + loginId + "님의 가계부 요약 === ✨🌸\n\n");
        area.append("💰 총 수입: " + totalIncome + "원\n");
        area.append("💸 총 지출: " + totalExpense + "원\n");
        area.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        area.append("💖 현재 잔액: " + balance + "원\n\n");
        
        if(balance > 0) {
            area.append("🎉✨ 와아! 흑자예요! 정말 잘하고 계시네요! 👏💕\n");
            area.append("🌟 이 조자로 계속 열심히 모아보세요! 💪😊\n");
        } else if(balance < 0) {
            area.append("😅💦 앗! 적자네요... 괜찮아요! 💪\n");
            area.append("🤗 다음달엔 지출을 조금 줄여보는 건 어떨까요? 💡\n");
        } else {
            area.append("⚖️✨ 딱 맞네요! 수입과 지출의 균형이 완벽해요! 👍💖\n");
        }
        
        area.append("\n🌈 === 최근 거래 내역 === 🌈\n");
        
        // 최근 수입 3개
        area.append("\n💰 최근 수입 (따끈따끈!):\n");
        if(incomeList.isEmpty()) {
            area.append("  🥺 아직 수입이 없어요... 용돈을 기록해보세요!\n");
        } else {
            int count = Math.min(3, incomeList.size());
            for(int i = Math.max(0, incomeList.size() - count); i < incomeList.size(); i++) {
                Income income = incomeList.get(i);
                area.append("  📅 " + income.date + " | +💰 " + income.amount + "원 | 🏷️ " + income.desc + "\n");
            }
        }
        
        // 최근 지출 3개
        area.append("\n💸 최근 지출 (어디갔니?):\n");
        if(expenseList.isEmpty()) {
            area.append("  😊 아직 지출이 없어요! 절약왕이시네요!\n");
        } else {
            int count = Math.min(3, expenseList.size());
            for(int i = Math.max(0, expenseList.size() - count); i < expenseList.size(); i++) {
                Expense expense = expenseList.get(i);
                area.append("  📅 " + expense.date + " | -💸 " + expense.amount + "원 | 🏷️ " + expense.desc + "\n");
            }
        }
        
        area.append("\n🌟 === 귀여운 가계부 팁! === 🌟\n");
        area.append("🎀 매일매일 기록하는 습관을 만들어요! ✨\n");
        area.append("🌈 작은 돈이라도 소중히 여기며 모아보세요! 💕\n");
        area.append("🌸 목표 금액을 정하고 달성해보는 재미도 있어요! 🎯\n");
        area.append("💖 가계부 쓰는 자신을 칭찬해주세요! 👏✨\n");
    }

    public static void main(String[] args) {
        SimpleFinanceGUI gui = new SimpleFinanceGUI();
        gui.setVisible(true);
    }
}