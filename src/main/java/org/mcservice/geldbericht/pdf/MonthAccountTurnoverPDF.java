package org.mcservice.geldbericht.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.TextStyle;
import java.util.Locale;

import javax.money.MonetaryAmount;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import org.mcservice.geldbericht.data.MonthAccountTurnover;
import org.mcservice.geldbericht.data.Transaction;
import org.mcservice.javafx.control.table.DefaultTableMonetaryAmountConverter;

import com.itextpdf.io.font.constants.*;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.Style;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Tab;
import com.itextpdf.layout.element.TabStop;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.TabAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.layout.property.VerticalAlignment;

public class MonthAccountTurnoverPDF {
	
	protected Long uid=null;
	@NotNull
	@OneToOne
	protected final MonthAccountTurnover month;
	protected final String fileName;
	protected byte[] pdf;
	protected boolean printed=false;
	
	protected static final Style headline=new Style();
	protected static final Style headerbolt=new Style();
	protected static final Style header=new Style();
	protected static final Style text=new Style();
	protected static DefaultTableMonetaryAmountConverter moneyFormatter = new DefaultTableMonetaryAmountConverter();
	
	static {
		PdfFont font,bolt;
		try {
			font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
			bolt = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		headline.setFont(bolt).setFontSize(8f/14*32);
		headerbolt.setFont(bolt).setFontSize(8f/14*14);
		header.setFont(bolt).setFontSize(8f/14*18);
		text.setFont(font).setFontSize(8f/14*14).setMargin(0).setPadding(0);
	}
	

	public MonthAccountTurnoverPDF(MonthAccountTurnover month) throws IOException {
		this.month=month;
		this.fileName=createFilename();
		createPdf();
	}


	private void createPdf() throws IOException {
		ByteArrayOutputStream stream=new ByteArrayOutputStream();
		
		PdfWriter writer = new PdfWriter(stream);
		PdfDocument pdf = new PdfDocument(writer);
		Document document = new Document(pdf);
		// Create a PdfFont
		
		document.setMargins(6.f*2.7f, 3.75f*2.7f, 5.5f*2.7f, 13f*2.7f);
		
		//This is for debugging. put the form in the background and you see if you reproduced
		PageSize pageSize = new PageSize(PageSize.A4);
		PdfCanvas canvas = new PdfCanvas(pdf.addNewPage());
        canvas.addImage(ImageDataFactory.create("/home/Sebastian/workspace/Geldbericht/example/[Unbenannt]_9F23.png"), pageSize, false);
        
        MonetaryAmount initialAssets=month.getInitialAssets();
        MonetaryAmount initialDebt=month.getInitialDebt();
        
		
		int i=0,j=1;
		// Add a Paragraph
		while(i<month.getTransactions().size()) {
			addPageHeader(document,j);
			
			Table table = new Table(UnitValue.createPercentArray(
					new float[] {0.121f, 0.121f, 0.06f, 0.03f, 0.05f, 0.07f, 0.03f, 0.03f, 0.04f, 0.06f, 0.08f, 0.29f}));
			table.setWidth(UnitValue.createPercentValue(100));
			table.setMarginTop(-5);
			
			addTableHeader(table);
			addBalanceRow(table,initialAssets,initialDebt);
			
			MonetaryAmount pagesumAssets=month.getInitialAssets().getFactory().setNumber(0).create();
	        MonetaryAmount pagesumDebt=month.getInitialDebt().getFactory().setNumber(0).create();
			
			do {
				if(i<month.getTransactions().size()) {
					addTransaction(table, month.getTransactions().get(i));
					pagesumAssets=pagesumAssets.add(month.getTransactions().get(i).getReceipts());
					pagesumDebt=pagesumDebt.add(month.getTransactions().get(i).getSpending());
				} else {
					addBlankRow(table);
				}
				++i;
			} while(i%30!=0);
			document.add(table);
			
			MonetaryAmount smallerAmountAssets,smallerAmountDebt;
			if(pagesumAssets.isGreaterThan(pagesumDebt)) {
				smallerAmountAssets=pagesumAssets.subtract(pagesumDebt);
				smallerAmountDebt=pagesumAssets.getFactory().setNumber(0).create();
			} else {
				smallerAmountAssets=pagesumAssets.getFactory().setNumber(0).create();
				smallerAmountDebt=pagesumDebt.subtract(pagesumAssets);
			}
			MonetaryAmount newBalance=initialAssets.subtract(initialDebt).add(smallerAmountAssets).subtract(smallerAmountDebt);
			if(newBalance.isPositiveOrZero()) {
				initialAssets=newBalance;
				initialDebt=pagesumAssets.getFactory().setNumber(0).create();
			} else {
				initialAssets=pagesumAssets.getFactory().setNumber(0).create();
				initialDebt=newBalance.multiply(-1L);
			}
			
			
			addPageFooter(document, initialAssets,initialDebt,pagesumAssets,pagesumDebt, smallerAmountAssets,smallerAmountDebt);
			j++;
		}
		document.close();
		
		this.pdf=stream.toByteArray();
	}
	
	protected void addPageHeader(Document document,int pageNumber) {
		Paragraph act=new Paragraph("Betr.-Nr.: ");
		act.addStyle(headerbolt)
		   .add(month.getAccount().getCompany().getCompanyNumber())
		   .add(new Tab()).addTabStops(new TabStop(350, TabAlignment.RIGHT))
	       .add(new Text("Geldbericht").addStyle(headline))
	       .add(new Tab()).addTabStops(new TabStop(500, TabAlignment.RIGHT))
	       .add("Monat: ").add(month.getMonth().getMonth().getDisplayName(TextStyle.FULL,Locale.GERMANY))
	       .add(" "+toString(month.getMonth().getYear()));

		act.setMargin(0);
		document.add(act);
		
		act=new Paragraph("Name: ");
		act.addStyle(header)
		   .add(month.getAccount().getCompany().getCompanyName());
		act.setMargin(0);
		document.add(act);
		
		act=new Paragraph();
		//Add the first part of the report
		Table table = new Table(new float[]{54f, 54f});
		table.setWidth(54*2);
		Cell cell = new Cell();
        cell.setMinHeight(9.1f*2.7f);
        cell.setVerticalAlignment(VerticalAlignment.BOTTOM);
        cell.setTextAlignment(TextAlignment.CENTER);
        cell.add(new Paragraph("KONTO:").addStyle(header));
		table.addCell(cell);
		cell = new Cell();
        cell.setVerticalAlignment(VerticalAlignment.BOTTOM);
        cell.setTextAlignment(TextAlignment.RIGHT);
        cell.add(new Paragraph(month.getAccount().getAccountNumber()).addStyle(headerbolt));
		table.addCell(cell);
		act.add(table);
		
		//Add the account name
		
		table = new Table(new float[]{130f});
		cell = new Cell().setTextAlignment(TextAlignment.LEFT);
        cell.add(new Paragraph(month.getAccount().getAccountName()).addStyle(header).setMargin(5));
        cell.setBorder(Border.NO_BORDER);
		table.addCell(cell);
		
		act.add(table);
		
		//Add the second table with accounting details, page and month as number
		table = new Table(new float[]{45.5f, 68.25f,56.875f,22.75f,22.75f,22.75f});
		table.setWidth(240f);
		//Add Table header
		cell = new Cell().setTextAlignment(TextAlignment.CENTER);
        cell.add(new Paragraph("Erf.-Nr.:").setMargin(-1).addStyle(text));
		table.addCell(cell);
		cell = new Cell().setTextAlignment(TextAlignment.CENTER);
        cell.add(new Paragraph("Land Buchstelle").setMargin(-1).addStyle(text));
		table.addCell(cell);
		cell = new Cell().setTextAlignment(TextAlignment.CENTER);
        cell.add(new Paragraph("Betriebs-Nr.:").setMargin(-1).addStyle(text));
		table.addCell(cell);
		cell = new Cell().setTextAlignment(TextAlignment.CENTER);
        cell.add(new Paragraph("Seite").setMargin(-1).addStyle(text));
		table.addCell(cell);
		cell = new Cell().setTextAlignment(TextAlignment.CENTER);
        cell.add(new Paragraph("Mon.").setMargin(-1).addStyle(text));
		table.addCell(cell);
		cell = new Cell().setTextAlignment(TextAlignment.CENTER);
        cell.add(new Paragraph("Jahr").setMargin(-1).addStyle(text));
        table.addCell(cell);
        
        //Add table data
        cell = new Cell().setMinHeight(17f).setTextAlignment(TextAlignment.CENTER).setVerticalAlignment(VerticalAlignment.MIDDLE);
        cell.add(new Paragraph(month.getAccount().getCompany().getCompanyBookkeepingAppointment().substring(0, 4)).addStyle(text));
		table.addCell(cell);
		cell = new Cell().setTextAlignment(TextAlignment.CENTER).setVerticalAlignment(VerticalAlignment.MIDDLE);
		cell.add(new Paragraph(month.getAccount().getCompany().getCompanyBookkeepingAppointment().substring(4)).addStyle(text));
		table.addCell(cell);
		cell = new Cell().setTextAlignment(TextAlignment.CENTER).setVerticalAlignment(VerticalAlignment.MIDDLE);
		cell.add(new Paragraph(month.getAccount().getCompany().getCompanyNumber()).addStyle(text));
		table.addCell(cell);
		cell = new Cell().setTextAlignment(TextAlignment.CENTER).setVerticalAlignment(VerticalAlignment.MIDDLE);
        cell.add(new Paragraph(String.format("%02d", pageNumber)).addStyle(text));
		table.addCell(cell);
		cell = new Cell().setTextAlignment(TextAlignment.CENTER).setVerticalAlignment(VerticalAlignment.MIDDLE);
		cell.add(new Paragraph(String.format("%02d", month.getMonth().getMonthValue())).addStyle(text));
		table.addCell(cell);
		cell = new Cell().setTextAlignment(TextAlignment.CENTER).setVerticalAlignment(VerticalAlignment.MIDDLE);
		cell.add(new Paragraph(Integer.toString(month.getMonth().getYear()).substring(2)).addStyle(text));
        table.addCell(cell);
        
        act.add(new Tab()).addTabStops(new TabStop(1e3f, TabAlignment.RIGHT));
        act.add(table);
		act.setMargin(0);
        
        
		document.add(act);
	}
	
	protected void addPageFooter(Document document, MonetaryAmount initialAssets, 
			MonetaryAmount initialDebt, MonetaryAmount pagesumAssets, MonetaryAmount pagesumDebt, 
			MonetaryAmount smallerAmountAssets, MonetaryAmount smallerAmountDebt) {
		Table table = new Table(UnitValue.createPercentArray(
				new float[] {0.2665f, 0.2665f, 0.4625f}));
		table.setWidth(UnitValue.createPercentValue(45.45f));
		table.setMarginTop(-4.5f);
		
		//Pagesum
		Cell cell = getTableCell();
		cell.add(new Paragraph(moneyFormatter.toString(pagesumAssets)).addStyle(text).setMultipliedLeading(1));
		table.addCell(cell);
		cell = getTableCell();
		cell.add(new Paragraph(moneyFormatter.toString(pagesumDebt)).addStyle(text).setMultipliedLeading(1));
		table.addCell(cell);
		cell = getTableCell();
		cell.add(new Paragraph("Aufrechnung (Seitensumme)").addStyle(text).setMultipliedLeading(1).setTextAlignment(TextAlignment.LEFT));
		table.addCell(cell);
		//substract smaller amount
		cell = getTableCell();
		cell.add(new Paragraph(moneyFormatter.toString(smallerAmountAssets)).addStyle(text).setMultipliedLeading(1));
		table.addCell(cell);
		cell = getTableCell();
		cell.add(new Paragraph(moneyFormatter.toString(smallerAmountDebt)).addStyle(text).setMultipliedLeading(1));
		table.addCell(cell);
		cell = getTableCell();
		cell.add(new Paragraph("abzüglich kleinerer Betrag").addStyle(text).setMultipliedLeading(1).setTextAlignment(TextAlignment.LEFT));
		table.addCell(cell);
		cell = getTableCell();
		//saldo of page
		cell.add(new Paragraph(moneyFormatter.toString(initialAssets)).addStyle(text).setMultipliedLeading(1));
		table.addCell(cell);
		cell = getTableCell();
		cell.add(new Paragraph(moneyFormatter.toString(initialDebt)).addStyle(text).setMultipliedLeading(1));
		table.addCell(cell);
		cell = getTableCell();
		cell.add(new Paragraph("Saldo der Seite").addStyle(text).setMultipliedLeading(1).setTextAlignment(TextAlignment.LEFT));
		table.addCell(cell);
		
		Paragraph act=new Paragraph().add(table);
		act.add(new Tab()).addTabStops(new TabStop(1000, TabAlignment.RIGHT));
		
		table=new Table(UnitValue.createPercentArray(
				new float[] {1f,.25f}));
		table.setWidth(UnitValue.createPercentValue(30f));
		table.setMarginRight(-1);
		
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<54;++i) {
			sb.append("\u00a0");
		}
		
		cell = getTableCell();
		cell.add(new Paragraph(sb.toString()).addStyle(text).setMultipliedLeading(1)
				.setUnderline());
		cell.setBorder(Border.NO_BORDER);
		table.addCell(cell);
		cell = getTableCell();
		cell.add(new Paragraph("20_____").addStyle(text).setMultipliedLeading(1));
		cell.setBorder(Border.NO_BORDER);
		table.addCell(cell);
		act.add(table);
		
		sb=new StringBuffer();
		for(int i=0;i<32;++i) {
			sb.append("\u00a0");
		}
		sb.append("Unterschrift");
		for(int i=0;i<32;++i) {
			sb.append("\u00a0");
		}
		
		cell = new Cell(1,2);
		cell.setMinHeight(20f);
		cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
		cell.setTextAlignment(TextAlignment.RIGHT);
		cell.add(new Paragraph(sb.toString()).addStyle(text).setMarginBottom(-32f).
				setUnderline(.75f,9f).setFontSize(7f));
		cell.setBorder(Border.NO_BORDER);
		table.addCell(cell);
		
		document.add(act);
	}
	
	protected void addTableHeader(Table table) {
		Paragraph paragraphs[]= {
				new Paragraph("(Einnahme, +\nmein Guthaben)"),
				new Paragraph("(Ausgabe, -\nmeine Schuld)"),
				new Paragraph("Gegen-\nkonto"),
				new Paragraph("KG"),
				new Paragraph("KST"),
				new Paragraph("Beleg"),
				new Paragraph("Tag"),
				new Paragraph("Mon."),
				new Paragraph("USt"),
				new Paragraph("Inv.-Nr.\nStück"),
				new Paragraph("Afa %"),
				new Paragraph("Gegenstand der Buchung")				
		};
		
		for (Paragraph paragraph : paragraphs) {
			Cell cell = new Cell();
	        cell.setMinHeight(38f);
	        cell.setVerticalAlignment(VerticalAlignment.BOTTOM);
	        cell.setTextAlignment(TextAlignment.CENTER);
	        cell.add(paragraph.addStyle(text).setMultipliedLeading(1));
			table.addCell(cell);
		}   
	}
	
	protected void addBalanceRow(Table table,MonetaryAmount initialAssets, MonetaryAmount initialDebt) {
		Cell cell = getTableCell();
		cell.add(new Paragraph(moneyFormatter.toString(initialAssets)).addStyle(text).setMultipliedLeading(1));
		table.addCell(cell);
		cell = getTableCell();
		cell.add(new Paragraph(moneyFormatter.toString(initialDebt)).addStyle(text).setMultipliedLeading(1));
		table.addCell(cell);
		
		cell = new Cell(1,9);
		cell.setMinHeight(5.675f*2.7f);
		cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
		cell.setTextAlignment(TextAlignment.LEFT);
		cell.add(new Paragraph("Übertrag/Kontostand").addStyle(header).setMultipliedLeading(1));
		table.addCell(cell);
		
		cell = getTableCell();
		cell.add(new Paragraph("").addStyle(text).setMultipliedLeading(1));
		table.addCell(cell);
		
		table.startNewRow();
	}
	
	protected void addTransaction(Table table, Transaction transaction) {
		
		Paragraph paragraphs[]= {
				new Paragraph(moneyFormatter.toString(transaction.getReceipts())), //1
				new Paragraph(moneyFormatter.toString(transaction.getSpending())),
				new Paragraph(toString(transaction.getAccountingContraAccount())),
				new Paragraph(toString(transaction.getAccountingCostGroup())),
				new Paragraph(toString(transaction.getAccountingCostCenter())), // 5
				new Paragraph(transaction.getVoucher()),
				new Paragraph(toString(transaction.getTransactionDate().getDayOfMonth())),
				new Paragraph(toString(transaction.getTransactionDate().getMonthValue())),
				new Paragraph(transaction.getVat().toString()),
				new Paragraph(toString(transaction.getInventoryNumber())), // 10
				new Paragraph(""),
				new Paragraph(transaction.getDescriptionOfTransaction()) //12		
		};
		
		for (Paragraph paragraph : paragraphs) {
			Cell cell = getTableCell();
	        cell.add(paragraph.addStyle(text).setMultipliedLeading(1));
			table.addCell(cell);
		}
		table.startNewRow();
	}

	protected void addBlankRow(Table table) {
		for (int i=0;i<12;++i) {
			table.addCell(getTableCell());
		}		
	}
	
	protected Cell getTableCell() {
		Cell cell = new Cell();
		cell.setMinHeight(5.675f*2.7f);
		cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
		cell.setTextAlignment(TextAlignment.RIGHT);
		return cell;
	}

	private String createFilename() {
		StringBuffer buff=new StringBuffer();
		buff.append(month.getAccount().getCompany().getCompanyName()).append(' ');
		buff.append(month.getAccount().getAccountName()).append(' ');
		buff.append(month.getMonth().getMonthValue()).append('.');
		buff.append(month.getMonth().getYear()).append(".pdf");
		return buff.toString();
	}
	
	private String toString(Integer i) {
		if (i==null)
			return "";
		else 
			return Integer.toString(i);
	}
	
	private String toString(Long i) {
		if (i==null)
			return "";
		else 
			return Long.toString(i);
	}
	
	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}


	/**
	 * @return the pdf
	 */
	public byte[] getPdf() {
		return pdf;
	}


	/**
	 * @return the printed
	 */
	public boolean isPrinted() {
		return printed;
	}


	/**
	 * @param printed the printed to set
	 */
	public void setPrinted(boolean printed) {
		this.printed = printed;
	}


}
