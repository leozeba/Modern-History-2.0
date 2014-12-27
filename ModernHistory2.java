import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.util.PDFOperator;

public class ModernHistory2 {
	
	Boolean remove = true;
	
	public static void main(String args[]) throws Exception {
		List<String> list = Arrays.asList(args);
		System.out.println("|-------------------------------------------------------------------------------|");
		System.out.println("| Modern History 2.0                                                            |");
		System.out.println("| A tribute to SARAH CHARLESWORTH by @leozeba                                   |");
		System.out.println("| http://www.modern-history-2.art.br                                            |");
		System.out.println("|-------------------------------------------------------------------------------|");		
		System.out.println("| \"I don't believe in art. I believe in artists.\" - Marcel Duchamp              |");		
		System.out.println("|-------------------------------------------------------------------------------|");
		
		if (list.contains("--newspaper") && list.contains("--edition")) {
			String newspaper = null;
			String edition = null;
			for (int i = 0; i < args.length; i++) {
				if ("--newspaper".equals(args[i])) newspaper = args[i + 1];
				if ("--edition".equals(args[i])) edition = args[i + 1];
			}
			if ("NYTimes".equals(newspaper)) {
				new ModernHistory2().NYTimes(edition);
			} else {
				System.out.println("Newspaper (" + newspaper + ") not implemented yet!");
			}
		} else {
			System.out.println("--newspaper and --edition are required.");
		}

		System.out.println("|-------------------------------------------------------------------------------|");		
		System.out.println("| \"Don't think about making art, just get it done.                              |");
		System.out.println("| Let everyone else decide if it's good or bad, whether they love it or hate it.|");
		System.out.println("| While they are deciding, make even more art.\" - Andy Warhol                   |");		
		System.out.println("|-------------------------------------------------------------------------------|");		
	}
	
	private void NYTimes(String edition) throws IOException, COSVisitorException {
		String filename = "NYTimes-" + edition.replace("/", "") + ".pdf";
		System.out.println("| Downloading...                                                                |");
		URL website = new URL("http://www.nytimes.com/images/" + edition + "/nytfrontpage/scan.pdf");
		InputStream in = new BufferedInputStream(website.openStream());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int n = 0;
		while (-1 != (n = in.read(buffer))) {
			out.write(buffer, 0, n);
		}
		out.close();
		in.close();
		byte[] response = out.toByteArray();
	 	FileOutputStream fos = new FileOutputStream(filename);
		fos.write(response);
		fos.close();		
		System.out.println("| Parsing...                                                                    |");
		PDFParser pdfParser = new PDFParser(new FileInputStream(new File(filename)));
		pdfParser.parse();
		PDDocument pdDocument = new PDDocument(pdfParser.getDocument());
		try {
			List pages = pdDocument.getDocumentCatalog().getAllPages();
			for (int i = 0; i < pages.size(); i++) {
				PDPage pdPage = (PDPage) pages.get(i);
				PDFStreamParser parser = new PDFStreamParser(pdPage.getContents().getStream());
				parser.parse();
				List tokens = parser.getTokens();
				for (int j = 0; j < tokens.size(); j++) {
					Object token = tokens.get(j);
					if (token instanceof PDFOperator) {
						PDFOperator pdfOperator = (PDFOperator) token;
						if (pdfOperator.getOperation().equals("Tj")) {
							process(tokens.get(j - 1));
						} else if (pdfOperator.getOperation().equals("TJ")) {
							COSArray cosArray = (COSArray) tokens.get(j - 1);
							for (int k = 0; k < cosArray.size(); k++) {
								Object element = cosArray.getObject(k);
								if (element instanceof COSString) {
									process(element);
								}
							}
						} 
					}
				}
				PDStream pdStream = new PDStream(pdDocument);
				new ContentStreamWriter(pdStream.createOutputStream()).writeTokens(tokens);
				pdPage.setContents(pdStream);
			}
			System.out.println("| Saving...                                                                     |");			
			pdDocument.save(new File(filename.replace(".pdf", "") + "-modern-history-2.pdf"));
		} finally {
			if (pdDocument != null) pdDocument.close();
		}			
	}

	private String process(Object token) throws IOException {
		COSString cosString = (COSString) token;
		String original = cosString.getString();
		String string = cosString.getString();
		if (original.equals("VOL.")) this.remove = false;
		if (this.remove) string = "";
		if (original.equals("$2.50") || original.equals("$5.00")) this.remove = true;		
		if (this.remove) {
			cosString.reset();
			cosString.append(string.getBytes());
		}
		return string;
	}
	
	
	private Boolean start(String string) {
		Boolean remove = false;
		try {
			if (string.startsWith("$")) {
				DecimalFormat df = new DecimalFormat();
				df.setPositivePrefix("$");
				df.parse(string);
				remove = true;
			}
		} catch (ParseException e) {
		}
		return remove;
	}
	
}