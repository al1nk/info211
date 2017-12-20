package tetris;

import java.awt.*;
import javax.swing.*;
import java.util.*;
import javax.swing.event.*;
@SuppressWarnings("serial")

public class JBrainTetris extends JTetris {

    private Brain brain;
    private Brain.Move bestMove;
    private int dernierI;
    private JCheckBox brainMode;
    private JCheckBox animation;
    private JPanel pan;
    private JSlider adversaire;
    private JLabel randomizedI;
    
    public static void main(String[] args) {
        JBrainTetris tetris = new JBrainTetris(16);
        JFrame frame = JBrainTetris.createFrame(tetris);
        frame.setVisible(true);
    }
    
    public JComponent createControlPanel() {
        JComponent panel = super.createControlPanel();
        panel.add(new JLabel("Brain:"));
        brainMode = new JCheckBox("Actif");
        animation = new JCheckBox("Animation");
        animation.setSelected(true);
        animation.setEnabled(false);
        panel.add(brainMode);
        panel.add(animation);
        pan = new JPanel();
        pan.add(new JLabel("Adversaire : "));
        adversaire = new JSlider(0, 100, 0);
        adversaire.setPreferredSize(new Dimension(100, 15));
        pan.add(adversaire);
        panel.add(pan);
        randomizedI = new JLabel("");
        panel.add(randomizedI);

        brainMode.addChangeListener(e -> {
            if(brainMode.isSelected()) {
                animation.setEnabled(true);
            } else {
                animation.setEnabled(false);
            }
        });
        
        return panel;
    }
    
    public JBrainTetris(int pixels) {
        super(pixels);
        brain = new DefaultBrain();
        bestMove = null;
        dernierI = count;
    }
    
    public void tick(int verbe) {
        if(brainMode.isSelected()) {
            board.undo();
            
            if(dernierI != count || bestMove == null) {
                bestMove = brain.bestMove(board, currentPiece, HEIGHT);
                dernierI = count;
            }
            
            if(verbe == DOWN && bestMove != null) {
                if(bestMove.x < currentX) {
                	super.tick(LEFT);
                } else if(bestMove.x > currentX) {
                	super.tick(RIGHT);
                }
                
                if(!bestMove.piece.equals(currentPiece)) {
                	super.tick(ROTATE);
                } else if(!animation.isSelected() && bestMove.x == currentX) {
                    super.tick(DROP);
                    super.tick(DOWN);
                }
            }
        }
        super.tick(verbe);
    }
    
    public Piece pickNextPiece() {
        if(random.nextInt(100) >= adversaire.getValue()) {
            randomizedI.setText("done");
            return super.pickNextPiece();
        }
        
        randomizedI.setText("*done*");
        return pickWorstPiece();
    }

   private Piece pickWorstPiece() {
       java.util.List<Piece> pieces = Arrays.asList(Piece.getPieces());
       Brain.Move worst = brain.bestMove(board, pieces.get(0), HEIGHT);
       for(Piece piece : pieces) {
           if(piece.equals(worst.piece)) {
        	   continue;
           }
           Brain.Move bigger = brain.bestMove(board, piece, HEIGHT);
           if(bigger.score > worst.score) {
        	   worst = bigger;
           }
       }
       return worst.piece;
   }
}
