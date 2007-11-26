/*
 * Created on 18-Oct-2007
 * Copyright (C) 2006 by Andrea Vacondio.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software Foundation; 
 * either version 2 of the License.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; 
 * if not, write to the Free Software Foundation, Inc., 
 *  59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.pdfsam.console.business.pdf.handlers;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.log4j.Logger;
import org.pdfsam.console.business.ConsoleServicesFacade;
import org.pdfsam.console.business.dto.commands.AbstractParsedCommand;
import org.pdfsam.console.business.dto.commands.MixParsedCommand;
import org.pdfsam.console.business.pdf.handlers.interfaces.AbstractCmdExecutor;
import org.pdfsam.console.exceptions.console.ConsoleException;
import org.pdfsam.console.exceptions.console.MixException;
import org.pdfsam.console.utils.FileUtility;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.RandomAccessFileOrArray;
/**
 * Command executor for the alternate mix command
 * @author Andrea Vacondio
 */
public class AlternateMixCmdExecutor extends AbstractCmdExecutor{

	private final Logger log = Logger.getLogger(AlternateMixCmdExecutor.class.getPackage().getName());
	
	public void execute(AbstractParsedCommand parsedCommand) throws ConsoleException {
		if((parsedCommand != null) && (parsedCommand instanceof MixParsedCommand)){
			MixParsedCommand inputCommand = (MixParsedCommand) parsedCommand;
			setWorkIndeterminate();
			Document pdfDocument = null;
			PdfCopy  pdfWriter = null;
			PdfReader pdfReader1;
			PdfReader pdfReader2;
		    int[] limits1 = {1,1};
		    int[] limits2 = {1,1};
			try{
				File tmpFile = FileUtility.generateTmpFile(inputCommand.getOutputFile());

				pdfReader1 = new PdfReader(new RandomAccessFileOrArray(inputCommand.getFirstInputFile().getFile().getAbsolutePath()),inputCommand.getFirstInputFile().getPassword().getBytes());
				pdfReader1.consolidateNamedDestinations();
				limits1[1] = pdfReader1.getNumberOfPages();

				pdfReader2 = new PdfReader(new RandomAccessFileOrArray(inputCommand.getSecondInputFile().getFile().getAbsolutePath()),inputCommand.getFirstInputFile().getPassword().getBytes());
				pdfReader2.consolidateNamedDestinations();
				limits2[1] = pdfReader2.getNumberOfPages();


				pdfDocument = new Document(pdfReader1.getPageSizeWithRotation(1));
				log.debug("Creating a new document.");
				pdfWriter = new PdfCopy(pdfDocument, new FileOutputStream(tmpFile));
				
				if(inputCommand.getOutputPdfVersion() != null){
					pdfWriter.setPdfVersion(inputCommand.getOutputPdfVersion().charValue());
				}
				
		        if(inputCommand.isCompress()){
		        	pdfWriter.setFullCompression();
		        }		
		        
				pdfDocument.addCreator(ConsoleServicesFacade.CREATOR);
				pdfDocument.open();

				PdfImportedPage page;

				boolean finished1 = false;
				boolean finished2 = false;
				int current1 = (inputCommand.isReverseFirst())? limits1[1] :limits1[0];
				int current2 = (inputCommand.isReverseSecond())? limits2[1] :limits2[0];
				while(!finished1 || !finished2){
					if(!finished1){
						if(current1>=limits1[0] && current1<=limits1[1]){
							page = pdfWriter.getImportedPage(pdfReader1, current1);
							pdfWriter.addPage(page);
							current1 = (inputCommand.isReverseFirst())? (current1-1) :(current1+1);
						}else{
							log.info("First file processed.");
							pdfReader1.close();							
							finished1 = true;
						}
					}
					if(!finished2){
						if(current2>=limits2[0] && current2<=limits2[1] && !finished2){
							page = pdfWriter.getImportedPage(pdfReader2, current2);
							pdfWriter.addPage(page);
							current2 = (inputCommand.isReverseSecond())? (current2-1) :(current2+1);
						}else{
							log.info("Second file processed.");
							pdfReader2.close();
							finished2 = true;
						}
					}

				}

				pdfWriter.freeReader(pdfReader1);
				pdfWriter.freeReader(pdfReader2);

				pdfDocument.close();
				// step 6: temporary buffer moved to output file
				FileUtility.renameTemporaryFile(tmpFile, inputCommand.getOutputFile(), inputCommand.isOverwrite());
				log.info("Alternate mix completed.");
			}catch(Exception e){    		
				throw new MixException(e);
			}finally{
				setWorkCompleted();
			}
		}else{
			throw new MixException(MixException.ERR_BAD_COMMAND);
		}
		
	}

}