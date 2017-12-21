package tetris;

import java.util.*;


/**
 * An immutable representation of a tetris piece in a particular rotation. Each
 * piece is defined by the blocks that make up its body.
 * 
 * Typical client code looks like...
 * 
 * <pre>
 * Piece pyra = new Piece(PYRAMID_STR); // Create piece from string
 * int width = pyra.getWidth(); // 3
 * Piece pyra2 = pyramid.computeNextRotation(); // get rotation
 * 
 * Piece[] pieces = Piece.getPieces(); // the array of all root pieces
 * </pre>
 */
public class Piece {

	// String constants for the standard 7 Tetris pieces
	public static final String STICK_STR = "0 0 0 1 0 2 0 3";
	public static final String L1_STR = "0 0 0 1 0 2 1 0";
	public static final String L2_STR = "0 0 1 0 1 1 1 2";
	public static final String S1_STR = "0 0 1 0 1 1 2 1";
	public static final String S2_STR = "0 1 1 1 1 0 2 0";
	public static final String SQUARE_STR = "0 0 0 1 1 0 1 1";
	public static final String PYRAMID_STR = "0 0 1 0 1 1 2 0";

	// Attributes
	private List<TPoint> body;
	private List<Integer> skirt;
	private int width;
	private int height;
	
	static private Piece[] pieces; // singleton static array of first rotations

	/**
	 * Defines a new piece given a TPoint[] array of its body. Makes its own
	 * copy of the array and the TPoints inside it.
	 */
	public Piece(List<TPoint> points) {
		this.body=points;
		
	    this.width=0;
	    for (int i=0; i<this.body.size(); i++) {
	        if(this.body.get(i).x > this.width) {
	        	this.width = this.body.get(i).x;
	        }
	    }
	    this.width++;
	    
	    this.height=0;
		for (int i=0; i<this.body.size(); i++) {
		    if(this.body.get(i).y > this.height) {
		    	this.height=this.body.get(i).y;
		    }
		}
		this.height++;
		
	    List<Integer> temp = new ArrayList<>();
	    this.skirt = new ArrayList<>();
	    for (int i=0; i<this.body.size(); i++) {
	    	if(!(temp.contains(this.body.get(i).x))) {
	    		this.skirt.add(this.body.get(i).x, this.body.get(i).y);
	    		temp.add(this.body.get(i).x);
	    	} else if(this.skirt.get(temp.indexOf(this.body.get(i).x))>this.body.get(i).y){
	    		this.skirt.remove(temp.indexOf(this.body.get(i).x));
	    		this.skirt.add(this.body.get(i).x, this.body.get(i).y);
	    	}
	    }
	}
	
	/**
	 * Alternate constructor, takes a String with the x,y body points all
	 * separated by spaces, such as "0 0 1 0 2 0 1 1". (provided)
	 */
	public Piece(String points) {
		this.body=parsePoints(points);
		
		this.width=0;
	    for (int i=0; i<this.body.size(); i++) {
	        if(this.body.get(i).x > this.width) {
	        	this.width = this.body.get(i).x;
	        }
	    }
	    this.width++;
	    
	    this.height=0;
		for (int i=0; i<this.body.size(); i++) {
		    if(this.body.get(i).y > this.height) {
		    	this.height=this.body.get(i).y;
		    }
		}
		this.height++;
		
	    List<Integer> temp = new ArrayList<>();
	    this.skirt = new ArrayList<>();
	    for (int i=0; i<this.body.size(); i++) {
	    	if(!(temp.contains(this.body.get(i).x))) {
	    		this.skirt.add(this.body.get(i).x, this.body.get(i).y);
	    		temp.add(this.body.get(i).x);
	    	} else if(this.skirt.get(temp.indexOf(this.body.get(i).x))>this.body.get(i).y){
	    		this.skirt.remove(temp.indexOf(this.body.get(i).x));
	    		this.skirt.add(this.body.get(i).x, this.body.get(i).y);
	    	}
	    }
	}

	public Piece(Piece piece) {
		this.body = new ArrayList<>(piece.getBody());
		
		this.width = piece.getWidth();
		
		this.height = piece.getHeight();
		
		this.skirt = new ArrayList<>(piece.getSkirt());
	}


	/**
	 * Given a string of x,y pairs ("0 0 0 1 0 2 1 0"), parses the points into a
	 * TPoint[] array. (Provided code)
	 */
	
	private static List<TPoint> parsePoints(String rep) {
		List<TPoint> points = new ArrayList<TPoint>();
		String[] s = rep.split(" ");
		try {
		for (int i=0; i<s.length; i=i+2) {
			int x = Integer.parseInt(s[i]);
			int y = Integer.parseInt(s[i+1]);
			points.add(new TPoint(x,y));
		}
		}
		catch (NumberFormatException e) {
			throw new RuntimeException("Could not parse x,y");
		}
		return points;
	}
	
	
	/**
	 * Returns the width of the piece measured in blocks.
	 */
	public int getWidth() {
		return this.width;
	}

	/**
	 * Returns the height of the piece measured in blocks.
	 */
	public int getHeight() {
		return this.height;
	}

	/**
	 * Returns a reference to the piece's body. The caller should not modify this
	 * list.
	 */
	public List<TPoint> getBody() {
		return this.body;
	}

	/**
	 * Returns a reference to the piece's skirt. For each x value across the
	 * piece, the skirt gives the lowest y value in the body. This is useful for
	 * computing where the piece will land. The caller should not modify this
	 * list.
	 */
	public List<Integer> getSkirt() {
		return this.skirt;
	}

	/**
	 * Returns a new piece that is 90 degrees counter-clockwise rotated from the
	 * receiver.
	 */
	
	public Piece computeNextRotation() {
		TPoint[] nPoints = new TPoint[this.body.size()];
		int temp=0;
		ArrayList<TPoint> mPoints = new ArrayList<>();
		this.body.toArray(nPoints);
		for (int i=0; i<nPoints.length; i++) {
			temp=nPoints[i].x;
			nPoints[i].x=nPoints[i].y;
			nPoints[i].y=temp;
		}
		for (int i=0; i<nPoints.length; i++) {
			nPoints[i].x=Math.abs(nPoints[i].x - (this.height-1));
		}
		
		for (int i=0; i<nPoints.length; i++) {
			if (nPoints[i].x==0) {
				int x = nPoints[i].x;
				int y = nPoints[i].y;
				mPoints.add(new TPoint(x,y));
			}
		}
		for (int i=0; i<nPoints.length; i++) {
			if (nPoints[i].x==1) {
				int x = nPoints[i].x;
				int y = nPoints[i].y;
				mPoints.add(new TPoint(x,y));
			}
		}
		for (int i=0; i<nPoints.length; i++) {
			if (nPoints[i].x==2) {
				int x = nPoints[i].x;
				int y = nPoints[i].y;
				mPoints.add(new TPoint(x,y));
			}
		}
		for (int i=0; i<nPoints.length; i++) {
			if (nPoints[i].x==3) {
				int x = nPoints[i].x;
				int y = nPoints[i].y;
				mPoints.add(new TPoint(x,y));
			}
		}
		return new Piece(mPoints);
	}
	
	/**
	 * Returns true if two pieces are the same -- their bodies contain the same
	 * points. Interestingly, this is not the same as having exactly the same
	 * body arrays, since the points may not be in the same order in the bodies.
	 * Used internally to detect if two rotations are effectively the same.
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof Piece)) {
			return false;
		}
		Piece other = (Piece)obj;
		
		List<TPoint> myBody = this.body;
		List<TPoint> otherBody = other.getBody();
		
		return myBody.containsAll(otherBody);
	}

	public String toString() {
		StringBuilder stringSkirt = new StringBuilder();
		for(int i=0; i<this.skirt.size(); i++) {
			 stringSkirt.append(Integer.toString(this.skirt.get(i))).append(" ");
		}
		
		StringBuilder stringBody = new StringBuilder();
		for(int i=0; i<this.body.size(); i++) {
			 stringBody.append(Integer.toString(this.body.get(i).x)).append(" ");
			 stringBody.append(Integer.toString(this.body.get(i).y)).append(" ");
		}
		
		String image = "Shape {\n";
	    image += "width=" + this.width + "; height=" + this.height + "; skirt=" + stringSkirt + "\n";
	    image += "body=" + stringBody + "\n";
	    char[][] grille = new char[this.height][this.width];
	   
	    for(int i=0; i<this.height; i++) {
	    	Arrays.fill(grille[i], ' ');
	    }

	    for(int i=0; i<this.body.size(); i++) {
	    	//grille[Math.abs(this.body.get(i).y - this.height + 1)][this.body.get(i).x] = 'X';
	    }
	    
	    StringBuilder s = new StringBuilder();
	    for(int i=0; i<this.height; i++) {
	    	s.append(new String(grille[i])).append("\n");
	    }
	    
	    image += s;
	    return image;
	}

	/**
	 * Returns an array containing the first rotation of each of the 7 standard
	 * tetris pieces in the order STICK, L1, L2, S1, S2, SQUARE, PYRAMID. The
	 * next (counterclockwise) rotation can be obtained from each piece with the
	 * {@link #fastRotation()} message. In this way, the client can iterate
	 * through all the rotations until eventually getting back to the first
	 * rotation. (provided code)
	 */
	public static Piece[] getPieces() {
		// lazy evaluation -- create static array if needed
		if (Piece.pieces == null) {
			Piece.pieces = new Piece[] { 
					new Piece(STICK_STR), 
					new Piece(L1_STR),
					new Piece(L2_STR), 
					new Piece(S1_STR),
					new Piece(S2_STR),
					new Piece(SQUARE_STR),
					new Piece(PYRAMID_STR)};
		}

		return Piece.pieces;
	}

}
