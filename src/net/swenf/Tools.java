package net.swenf;

import java.io.IOException;
import java.util.LinkedList;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import net.swenf.fonts.Fonts;

/**
 * Just some handy tools for page layout and displaying text.
 * 
 * @author Felix Batusic
 *
 */
public class Tools {

    public static void main(String[] args) throws IOException {
	String s = "blabla";
	PDDocument doc = new PDDocument();
	PDFont font = PDType0Font.load(doc, Fonts.class.getResourceAsStream("CourierPrime.ttf"));
	LinkedList<String> sl = splitStringForWidth(s, 300, font, 12);
	for (String x : sl) {
	    System.out.println(x);
	}
		
    }

    /**
     * Returns an array with the positions to display a table/columns on a page.
     * <br/>
     * For example, if you split the page in 3 columns, the return array will
     * have length 3 with the leftmost position in [0], next in [1] and so on.
     * <br/><br/>
     * If you don't wish to use custom margins, you can pass xPos as marginLeft.
     * 
     * @param pageWidth
     * @param marginLeft
     * @return positions of column starts
     */
    public static float[] splitPage(float pageWidth, float marginLeft, int columns) {
	float[] positions = new float [columns];
	for (int i = 0; i<columns; i++) {
	    positions[i]  = marginLeft + i*(pageWidth/columns);
	}
	return positions;
    }

    /**
     * Returns a list of Strings fitting for given width, font and font-size.
     * 
     * @param text
     * @param width
     * @param font
     * @param fontSize
     * @return list of Strings fitting for given width, font and font-size
     */
    public static LinkedList<String> splitStringForWidth(String text, float width, PDFont font, float fontSize) {
	try {
	    String[] split = text.split(" ");
	    if (split.length > 0) {
		LinkedList<String> sList = new LinkedList<>();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < split.length; i++) {
		    float textWidth = font.getStringWidth(sb.toString()) / 1000 * fontSize;
		    float nextWidth = font.getStringWidth(split[i]) / 1000 * fontSize;
		    
		    if ((textWidth + nextWidth) > width) {
			sList.add(sb.toString());
			sb = new StringBuilder();
		    }
		    
		    if (i < split.length - 1) {
			sb.append(split[i] + " ");
		    } else {
			sb.append(split[i]);
		    }
		    
		    if (i == split.length - 1) {
			sList.add(sb.toString());
		    }
		}
		return sList;
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return null;
    }
}
