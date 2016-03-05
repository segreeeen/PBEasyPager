package net.swenf.pager;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import net.swenf.fonts.Fonts;

/**
 * This class controls all the printing. Add your pager(s) with the addPager()
 * method, and call finalizeDocument() when you are done writing your content in
 * them. See AbstractPager for an explanation on how to do that.
 * 
 * @author Felix Batusic
 *
 */
public class PagerController {

    public enum FormattingType {
	NORMAL, ITALIC, BOLD, BOLDITALIC, UNDERLINED;
    }

    // Page Layout
    static final Color STANDARD_TEXT_COLOR = Color.BLACK;
    static final int STANDARD_FONT_SIZE = 12;
    static final float LINE_HEIGHT_FACTOR = 1.2f;
    static final float UNDER_LINE_FACTOR = 1.1f;
    static final float UNDER_LINE_CORRECTION = 3f;

    // Dual Constants
    protected static final int FIRST = 1;
    protected static final int SECOND = 2;

    // Document
    private PDDocument doc;
    private ArrayList<AbstractPager<?>> pagers;

    // Fonts
    PDFont font;
    PDFont boldFont;
    PDFont italicFont;
    PDFont boldItalicFont;

    // Margins
    final float marginTop;
    final float marginLeft;
    final float marginRight;
    final float marginBottom;

    /**
     * You can set the margins (space to top/bottom/left/right here. It's not possible to change the Margins. If
     * you need different Margins for different Pagers you have to set them to 0
     * here and handle your margins yourself.
     * 
     * @param marginLeft
     * @param marginRight
     * @param marginTop
     * @param marginBottom
     * @throws IOException
     */
    public PagerController(float marginLeft, float marginRight, float marginTop, float marginBottom) throws IOException {
	// set values defined by user
	this.doc = new PDDocument();
	this.marginLeft = marginLeft;
	this.marginRight = marginRight;
	this.marginTop = marginTop;
	this.marginBottom = marginBottom;

	// initialize standard values.
	// note that it should be possible to set a custom font at a later time.
	this.font = PDType0Font.load(doc, Fonts.class.getResourceAsStream("CourierPrime.ttf"));
	this.boldFont = PDType0Font.load(doc, Fonts.class.getResourceAsStream("CourierPrimeBold.ttf"));
	this.italicFont = PDType0Font.load(doc, Fonts.class.getResourceAsStream("CourierPrimeItalic.ttf"));
	this.boldItalicFont = PDType0Font.load(doc, Fonts.class.getResourceAsStream("CourierPrimeBoldItalic.ttf"));

	this.pagers = new ArrayList<>();
    }

    /**
     * Add a Pager. Be aware that the pagers will be appended to the final PDF
     * in the order they were added here.
     * 
     * @throws IOException
     */
    public void addPager(AbstractPager<?> pager) throws IOException {
	pagers.add(pager);
    }

    /**
     * Call this to write the content of the pagers to the PDF.
     * 
     * @param fileName
     * @throws IOException
     */
    public void finalizeDocument(String fileName) throws IOException {
	if (pagers.isEmpty()) {
	    throw new IllegalStateException("There are no pagers to be written.");
	}

	for (AbstractPager<?> pager : pagers) {
	    pager.closeStream();

	    PDPageTree pages = pager.getPages();
	    pager.closeStream();
	    for (PDPage p : pages) {
		doc.addPage(p);
	    }
	}

	doc.save(fileName);
	doc.close();
	closePagers();
    }

    /**
     * Use this method to set all Fonts at once.
     * 
     * @param font
     *            - path to font
     * @param boldFont
     *            - path to bold font
     * @param italicFont
     *            - path to italic font
     * @param boldItalicFont
     *            - path to bold italic font
     * @throws IOException
     */
    public void setFonts(String font, String boldFont, String italicFont, String boldItalicFont) throws IOException {
	setFont(font);
	setBoldFont(boldFont);
	setItalicFont(boldItalicFont);
	setBoldItalicFont(boldItalicFont);
    }

    /**
     * Set the normal font.
     * 
     * @param font
     *            - path to font
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void setFont(String font) throws FileNotFoundException, IOException {
	this.font = PDType0Font.load(doc, new FileInputStream(font));
    }

    /**
     * Set bold font.
     * 
     * @param boldFont
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void setBoldFont(String boldFont) throws FileNotFoundException, IOException {
	this.boldFont = PDType0Font.load(doc, new FileInputStream(boldFont));
    }

    /**
     * Set italic font.
     * 
     * @param italicFont
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void setItalicFont(String italicFont) throws FileNotFoundException, IOException {
	this.italicFont = PDType0Font.load(doc, new FileInputStream(italicFont));
    }

    /**
     * Set bold, italic font.
     * 
     * @param boldItalicFont
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void setBoldItalicFont(String boldItalicFont) throws FileNotFoundException, IOException {
	this.boldItalicFont = PDType0Font.load(doc, new FileInputStream(boldItalicFont));
    }

    private void closePagers() throws IOException {
	for (AbstractPager<?> p : pagers) {
	    p.close();
	}
    }

}
