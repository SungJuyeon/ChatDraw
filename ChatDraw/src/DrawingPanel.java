import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;


public class DrawingPanel extends JPanel {
    private Color brushColor = Color.BLACK;
    private int brushSize = 6;
    private boolean eraserMode = false;

    private int prevX = -1, prevY = -1;

    // 그린 그림을 저장할 리스트
    private List<Shape> shapes = new ArrayList<>();

    // TestFrame을 참조하기 위한 변수
    private TestFrame testFrame;

    // 생성자에 TestFrame을 전달받도록 수정
    public DrawingPanel(TestFrame testFrame) {
        this.testFrame = testFrame;
        setBackground(Color.WHITE);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                prevX = e.getX();
                prevY = e.getY();
                draw(e.getX(), e.getY(), true);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                prevX = -1; // 드래그 종료 후 초기화
                prevY = -1;
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                draw(e.getX(), e.getY(), false);
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // 부모의 paintComponent 호출

        // 그려진 모든 그림을 다시 그리기
        Graphics2D g2d = (Graphics2D) g;
        for (Shape shape : shapes) {
            g2d.setColor(shape.color);
            g2d.setStroke(new BasicStroke(shape.size, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            if (shape.type == Shape.ShapeType.LINE) {
                g2d.drawLine(shape.x1, shape.y1, shape.x2, shape.y2);
            } else if (shape.type == Shape.ShapeType.OVAL) {
                g2d.fillOval(shape.x1 - shape.size / 2, shape.y1 - shape.size / 2, shape.size, shape.size);
            }
        }
    }

    // Brush 색상 설정
    public void setBrushColor(Color color) {
        this.brushColor = color;
        if (eraserMode) {
            this.brushColor = Color.WHITE;
        }
    }
    
    public Color getBrushColor() {
    	return brushColor;
    }

    // Brush 크기 설정
    public void setBrushSize(int size) {
        this.brushSize = size;
    }

    // Eraser 모드 설정
    public void setEraserMode(boolean eraserMode) {
        this.eraserMode = eraserMode;
        if (eraserMode) {
            this.brushColor = Color.WHITE;
        } else {
            this.brushColor = Color.BLACK;
        }
    }

    // 그리기 기능
    private void draw(int x, int y, boolean isSinglePoint) {
        // 그리기 작업을 shapes 리스트에 저장
        if (isSinglePoint || prevX == -1 || prevY == -1) {
            shapes.add(new Shape(Shape.ShapeType.OVAL, prevX, prevY, x, y, brushColor, brushSize));
        } else {
            shapes.add(new Shape(Shape.ShapeType.LINE, prevX, prevY, x, y, brushColor, brushSize));
        }
        prevX = x;
        prevY = y;

        repaint(); // 그린 내용을 다시 그리기
    }

    public boolean isEraserMode() {
        return eraserMode;
    }

    // 캔버스를 비우는 기능
    public void clearCanvas() {
        shapes.clear();
        repaint(); // 그린 내용 지우기
    }

    // 캔버스를 이미지로 저장하는 메서드
    public void saveCanvas(TestFrame frame) {
        // 원본 크기 가져오기
        int originalWidth = getWidth();
        int originalHeight = getHeight();

        // 축소 크기 계산 (50%)
        int targetWidth = originalWidth / 2;
        int targetHeight = originalHeight / 2;

        // BufferedImage 생성 (축소 크기로)
        BufferedImage image = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // 스케일링하여 캔버스 내용 캡처
        g2d.scale(0.5, 0.5); // 50% 축소
        paintComponent(g2d); // 그려진 내용을 캡처
        g2d.dispose();

        // 저장할 폴더 경로
        File folder = new File("saved_images");
        if (!folder.exists()) {
            folder.mkdir(); // 폴더가 없으면 생성
        }

        // 파일 이름 자동 증가
        int iCount = 1;
        File savedFile = new File(folder, "drawing" + iCount + ".png");
        while (savedFile.exists()) {
            iCount++;
            savedFile = new File(folder, "drawing" + iCount + ".png");
        }

        // 이미지 저장
        try {
            ImageIO.write(image, "PNG", savedFile);
            System.out.println("그림이 저장되었습니다: " + savedFile.getAbsolutePath());

            // 이미지 저장 후 TestFrame 갱신
            if (frame != null) {
                frame.refreshImage();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // 그림 정보를 저장할 클래스
    private static class Shape {
        enum ShapeType { LINE, OVAL }

        ShapeType type;
        int x1, y1, x2, y2;
        Color color;
        int size;

        Shape(ShapeType type, int x1, int y1, int x2, int y2, Color color, int size) {
            this.type = type;
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.color = color;
            this.size = size;
        }
    }
}

