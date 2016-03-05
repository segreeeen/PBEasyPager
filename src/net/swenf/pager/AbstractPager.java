package net.swenf.pager;

import java.awt.Color;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.font.PDFont;


/**
 * A pager is used to define the layout of a page. This is the base for the
 * pagers. The printContent() method is the only method that needs to be
 * implemented in the pagers directly. <br/>
 * You can create as many pagers as you want, but make sure you add them to the
 * controller in the right order. </br>
 * Be aware that there is only one parameter for the printContent() method,
 * meaning that you need to marshal the content accordingly.
 * 
 * @author Felix Batusic
 *
 * @param <T>
 *            Type of the object, the parameter of the printContent method
 *            will have.
 */
public abstract class AbstractPager<T> implements Pager {
    enum Alignment {
	LEFT, CENTERED, RIGHT;
    }

    protected Class<? extends AbstractPager<?>> type;
    protected PagerController controller;

    protected PDDocument document;
    protected PDPage currentPage;
    protected PDPageContentStream stream;

    private Integer fontSize;
    private Color color;
    protected float yPos;
    protected float xPos;

    protected AbstractPager(PagerController controller) throws IOException {
	this.controller = controller;
	this.fontSize = null;
	this.document = new PDDocument();
	this.nextPage();
	this.xPos = getMarginLeft();
	this.yPos = getPageHeight() - getMarginTop();
	this.color = null;
    }

    PDDocument getDoc() {
	return this.document;
    }

    void closeStream() throws IOException {
	stream.stroke();
	stream.close();
    }

    void close() throws IOException {
	this.document.close();
    }

    PDPageTree getPages() {
	return document.getPages();
    }

    /**
     * Initiates the next page. Use yExceeded to check if it is necessary if you
     * are using the yPos system to control paging.
     * 
     * @throws IOException
     */
    protected void nextPage() throws IOException {
	if (currentPage != null) {
	    stream.close();
	}

	this.currentPage = new PDPage();
	this.document.addPage(currentPage);
	this.stream = new PDPageContentStream(document, currentPage);
	this.yPos = getPageHeight() - getMarginTop();
    }

    /**
     * Initiates the next line.
     * 
     * @param marginTop
     *            additional margin to add to the line.
     * @throws IOException
     */
    protected void nextLine(Float marginTop) throws IOException {
	if (marginTop != null) {
	    this.xPos = getMarginLeft();
	    this.yPos -= (getLineHeight() + marginTop - PagerController.UNDER_LINE_CORRECTION);
	} else {
	    this.xPos = getMarginLeft();
	    this.yPos -= (getLineHeight() - PagerController.UNDER_LINE_CORRECTION);
	}
    }

    /**
     * Initiates the next line.
     * 
     * @throws IOException
     */
    protected void nextLine() throws IOException {
	nextLine(null);
    }

    /**
     * Adds a marginBottom to y, use this to increase yPos after nextLine().
     * 
     * @param marginBottom
     *            height of margin
     */
    protected void finishLine(float marginBottom) {
	yPos -= marginBottom;
    }

    /**
     * Prints a text beginning at (x/y).
     * 
     * @param text
     * @param x
     *            x position.
     * @param y
     *            You can use yPos to control the Y position, by calling
     *            nextLine. If you opt to do that, you should probably use yPos
     *            as y parameter here.
     * @param font
     *            Use getFont(), getBoldFont() etc., to set the font you want
     *            to use.
     * @param fontSize
     *            Use getFontSize() as size, see getFontSize() for details.
     * @param color
     *            Use getColor() as color. See getColor() for details.
     * @throws IOException
     */
    protected void printString(String text, float x, float y, PDFont font, int fontSize, Color color) throws IOException {
	stream.beginText();
	setTextOptions(x, y, font, fontSize, color);
	stream.showText(text);
	stream.endText();
	stream.stroke();
    }

    /**
     * Returns true if there is not enough space for another line on this page -
     * given you are using yPos and nextLine() for line control.
     * 
     * @param heightAddition
     *            height addition is additionally subtracted from yPos,
     *            meaning you can check if an amount of space is free
     * @return True if there is not enough space for another line on this page
     */
    protected boolean yExceeded(float heightAddition) {
	if ((yPos - getMarginBottom() - getLineHeight() - heightAddition) < 0) {
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * Returns true if there is not enough space for another line on this page -
     * given you are using yPos and nextLine() for line control.
     * 
     * @return True if there is not enough space for another line on this page
     */
    protected boolean yExceeded() {
	return yExceeded(0);
    }

    /**
     * Check if width exceeds x.
     * 
     * @param stringWidth
     *            use stringWidth() to determine width of String.
     * @param marginRight
     * @return true if x is exceeded, false otherwise
     */
    protected boolean xExceeded(float stringWidth, float marginRight) {
	if ((xPos + stringWidth + getMarginRight() + marginRight) >= getPageWidth()) {
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * Check if width exceeds x.
     * 
     * @param stringWidth
     *            use stringWidth() to determine width of String.
     * @return true if x is exceeded, false otherwise
     */
    protected boolean xExceeded(float stringWidth) {
	return xExceeded(stringWidth, 0);
    }

    /**
     * Difference added
     * 
     * @return
     */
    protected float getUnderLineDifference() {
	return controller.font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * getFontSize() * PagerController.UNDER_LINE_FACTOR - getLineHeight();
    }

    /**
     * printContent should handle the printing. The most common scenario is to
     * define T as a list of objects which contain all values you want to
     * display, and then display them accordingly one by one.
     * <br/><br/>
     * Call printString() to write strings to the document.
     * 
     * @param t
     * @throws IOException
     */
    public abstract void printContent(T t) throws IOException;

    /**
     * Returns the current line height in respect to current fontsize.
     */
    public float getLineHeight() {
	return controller.font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * PagerController.STANDARD_FONT_SIZE * PagerController.LINE_HEIGHT_FACTOR;
    }

    /**
     * Returns the current line height in respect to given fontsize.
     */
    public float getLineHeight(int fontSize) {
	return controller.font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize * PagerController.LINE_HEIGHT_FACTOR;
    }

    /**
     * Returns the with of the string in respect to the given font and the
     * current fontSize.
     * 
     * @param text
     * @param font
     * @return  with of the string in respect to the given font and the current
     *         fontSize.
     * @throws IOException
     */
    public float stringWidth(String text, PDFont font) throws IOException {
	return font.getStringWidth(text) / 1000 * getFontSize();
    }

    /**
     * Returns center of the page in respect to given margins.
     */
    public float getPageCenter() {
	return getPageWidth() / 2;
    }

    /**
     * Returns the width of the page in respect to given margins.
     */
    public float getPageWidth() {
	return currentPage.getMediaBox().getWidth() - getMarginLeft() - getMarginRight();
    }

    /**
     * Returns the width of the page, ignoring margins.
     */
    public float getAbsoluteWidth() {
	return currentPage.getMediaBox().getWidth();
    }

    /**
     * Returns the height of the page.
     */
    public float getPageHeight() {
	return currentPage.getMediaBox().getHeight();
    }

    /**
     * Returns the margin to the top.
     */
    public float getMarginTop() {
	return controller.marginTop;
    }

    /**
     * Returns the margin to the left.
     */
    public float getMarginLeft() {
	return controller.marginLeft;
    }

    /**
     * Returns the margin to the right.
     */
    public float getMarginRight() {
	return controller.marginRight;
    }

    /**
     * Returns the margin to the bottom.
     */
    public float getMarginBottom() {
	return controller.marginBottom;
    }

    /**
     * Returns normal font.
     */
    public PDFont getFont() {
	return controller.font;
    }

    /**
     * Returns bold font.
     */
    public PDFont getBoldFont() {
	return controller.boldFont;
    }

    /**
     * Returns italic font.
     */
    public PDFont getItalicFont() {
	return controller.italicFont;
    }

    /**
     * Returns bitalic font.
     */
    public PDFont getBoldItalicFont() {
	return controller.boldItalicFont;
    }

    /**
     * Returns the standard fontsize if fontSize is null, or this.fontSize if != null.
     */
    public int getFontSize() {
	if (fontSize != null) {
	    return fontSize;
	} else {
	    return PagerController.STANDARD_FONT_SIZE;
	}
    }
    
    /**
     * Set the fontsize. Set to null to get the standard fontsize from getFontSize().
     * @param fontSize
     */
    public void setFontSize(Integer fontSize) {
	this.fontSize = fontSize;
    }

    /**
     * Returns the standard color, if color is null, else returns this.color
     */
    public Color getColor() {
	if (color != null) {
	    return color;
	} else {
	    return PagerController.STANDARD_TEXT_COLOR;
	}
    }

    /**
     * Set the color. Set to null to get the standard color (black) from getColor().
     * @param color 
     */
    public void setColor(Color color) {
	this.color = color;
    }

    /**
     * Draws a line from (x/y) to (x2/y2) 
     */
    public void drawLine(float x, float y, float x2, float y2) throws IOException {
	stream.setLineWidth(0.1f);
	stream.moveTo(x, y);
	stream.lineTo(x2, y2);
	stream.stroke();
    }

    /**
     * Prints the pagenumber at position x, positioning y slightly under marginbottom.
     * @param x
     * @throws IOException
     */
    public void printPageNumber(float x) throws IOException {
	String nr = Integer.toString(document.getNumberOfPages());
	float nrWidth = getFont().getStringWidth(nr) / 1000 * getFontSize();
	printString(nr, (getAbsoluteWidth() / 2) - (nrWidth / 2), getMarginBottom() - (getLineHeight()), getFont(), getFontSize(), getColor());
    }

    
    private void setTextOptions(float x, float y, PDFont font, int fontSize, Color color) throws IOException {
	stream.newLineAtOffset(x, y);
	stream.setNonStrokingColor(getColor());
	stream.setFont(font, fontSize);
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	AbstractPager<?> other = (AbstractPager<?>) obj;
	if (type != other.type)
	    return false;
	return true;
    }
}
