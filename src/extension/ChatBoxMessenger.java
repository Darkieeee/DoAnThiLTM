package extension;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Arrays;

public class ChatBoxMessenger extends javax.swing.JPanel{
    private final javax.swing.JScrollPane scrollPane;
    private final javax.swing.JPanel container;
    public static final int LEFT_MESSAGE = 1;
    public static final int RIGHT_MESSAGE = 2;
    public ChatBoxMessenger(){
        this.setLayout(new java.awt.BorderLayout());
        container = new javax.swing.JPanel();
        scrollPane = new javax.swing.JScrollPane();
        scrollPane.getViewport().add(container);
        scrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        this.add(scrollPane,java.awt.BorderLayout.CENTER);
        container.setLayout(new javax.swing.BoxLayout(container,
                                                      javax.swing.BoxLayout.Y_AXIS));
    }
    public void addChat(String nickname, String messageContent,int corner)
    {
        if(corner == LEFT_MESSAGE)
        {
            LeftBubbleChat leftMessage = new LeftBubbleChat(nickname, messageContent);
            container.add(leftMessage);
        }
        else if(corner == RIGHT_MESSAGE)
        {
            RightBubbleChat rightMessage = new RightBubbleChat(nickname, messageContent);
            container.add(rightMessage);
        }
    }
    public static void main(String... args)
    {
        javax.swing.JFrame frame = new javax.swing.JFrame("Test");
        frame.setLayout(new java.awt.BorderLayout());frame.repaint();
        frame.setPreferredSize(new java.awt.Dimension(500,300));
        frame.pack();
        frame.setLocationRelativeTo(null);
        ChatBoxMessenger cbm = new ChatBoxMessenger();
        frame.add(cbm,java.awt.BorderLayout.CENTER);
        frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        
    }
}
class RightBubbleChat extends javax.swing.JPanel{
    private javax.swing.JLabel lblNickname;
    private javax.swing.JLabel lblMessageContent;
    public RightBubbleChat(String nickname, String messageContent){
        lblNickname = new javax.swing.JLabel(nickname);
        lblMessageContent = new javax.swing.JLabel(messageContent);
        this.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
    }
    @Override
    public void paintComponent(java.awt.Graphics g)
    {
        final java.awt.Graphics2D graphics2D = (java.awt.Graphics2D) g;
        java.awt.RenderingHints qualityHints = new java.awt.RenderingHints(java.awt.RenderingHints.KEY_ANTIALIASING, 
                                                                           java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        qualityHints.put(java.awt.RenderingHints.KEY_RENDERING, 
                         java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        graphics2D.setRenderingHints(qualityHints);
        g.setColor(java.awt.Color.BLUE);
        g.fillRoundRect(0 ,0, 30,20, 2, 2);
    }
    public void setNickname(String nickname)
    {
        
    }
}
class LeftBubbleChat extends javax.swing.JPanel{
    private final javax.swing.JLabel lblNickname;
    private final javax.swing.JTextArea txaMessageContent;
    private final int strokeThickness = 5;
    private final int padding = strokeThickness / 2;
    private final int paddingText = 10;
    private final int radius = 10;
    private final int arrowSize = 4;
    private final int maxWidth = 200;
    public LeftBubbleChat(String nickname, String messageContent){
        super(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        lblNickname = new javax.swing.JLabel(nickname);
        txaMessageContent = new javax.swing.JTextArea(messageContent);
        this.setAlignmentX(java.awt.Component.RIGHT_ALIGNMENT);
        txaMessageContent.setRows(5);
        txaMessageContent.setWrapStyleWord(true);
        txaMessageContent.setLineWrap(true);
    }
    @Override
        public void paintComponent(final Graphics g) {
            final Graphics2D graphics2D = (Graphics2D) g;
            RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics2D.setRenderingHints(qualityHints);
            graphics2D.setColor(new Color(80, 150, 180));
            graphics2D.setStroke(new BasicStroke(strokeThickness));
            int x = padding + strokeThickness + arrowSize;
            FontMetrics fontMetrics = g.getFontMetrics();
            int width = fontMetrics.stringWidth(txaMessageContent.getText()) + paddingText - arrowSize - (strokeThickness * 2);
            int height = fontMetrics.getHeight()+paddingText - strokeThickness;
            graphics2D.fillRect(x, padding, width, height);
            RoundRectangle2D.Double rect = new RoundRectangle2D.Double(x, padding, width, height, radius, radius);
            Polygon arrow = new Polygon();
            arrow.addPoint(14, 4);
            arrow.addPoint(arrowSize + 2, 8);
            arrow.addPoint(14, 10);
            Area area = new Area(rect);
            area.add(new Area(arrow));
            graphics2D.draw(area);
            graphics2D.setColor(Color.BLACK);
            final String fullText = txaMessageContent.getText();
final ArrayList lines = new ArrayList();

StringBuilder sb = new StringBuilder(); 
for(final Character c : fullText.toCharArray()) {
    sb.append(c);
    if(fontMetrics.stringWidth(sb.toString()) > maxWidth) { 
        sb.setLength(sb.length() - 1); 
        lines.add(sb.toString()); 
        sb = new StringBuilder(c.toString()); 
    }
}
lines.add(sb.toString());
    System.out.println(lines);
    graphics2D.dispose();
}
}