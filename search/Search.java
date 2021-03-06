package search;

import java.io.*;

import java.util.Hashtable;

import search.data.DocumentReader;
import search.data.TDTReader;
import search.data.WebDocumentReader;

/**
 * A search class that will allow us to run queries against the TDT corpus.
 * It supports BOTH boolean and ranked queries.
 * 
 * @author dkauchak
 *
 */
public class Search{
	
	private Index index;
	private Hashtable<Integer,Document> id2doc = null;
	private Hashtable<Integer,WebResultData> id2web = null;
	private Tokenizer tokenizer;
	private TokenProcessor tokenProcessor;
	
	public static void main(String[] args){
		//TDTReader reader = new TDTReader("/Users/dave/data/TDT_Pilot_Corpus/tdt-corpus.text_only");
		//TDTReader reader = new TDTReader("/Users/dave/classes/cs160/assignments/assign1/sample_data/tdt-corpus.sample");
		TDTReader reader = new TDTReader("/Users/dave/classes/cs160/assignments/assign3/data/test_file");
		ImprovedTokenizer tokenizer = new ImprovedTokenizer();
		TokenProcessor processor = new TokenProcessor();				
		processor.setLowercase(true);
		reader.setTokenizer(tokenizer);
		reader.setTokenProcessor(processor);
		
		Search system = new Search(reader, tokenizer, processor, Index.TERM_MODIFIER.l, Index.DOC_MODIFIER.t, Index.LENGTH_MODIFIER.n);
		
		//Subsitituting the code below for the line above this would result in documents being returned rather
		// than document ids
		// TDTReader reader2 = new TDTReader("/Users/dave/data/TDT_Pilot_Corpus/tdt-corpus.text_only");
		// reader2.setTokenizer(tokenizer);
		// reader2.setTokenProcessor(processor);
		// Search system = new Search(reader, reader2,
		//		 Index.TERM_MODIFIER.l, Index.DOC_MODIFIER.t, Index.LENGTH_MODIFIER.c);
		
		system.run();
	}
	
	/*
	// Use this main function if you want to run with a web data file
	public static void main(String args[]){
		String web_data_file = "/Users/dkauchak/classes/cs458/final_project/code/cs458-f12/data/web_data.test";
		
		WebDocumentReader reader = new WebDocumentReader(web_data_file);
		ImprovedTokenizer tokenizer = new ImprovedTokenizer();
		TokenProcessor processor = new TokenProcessor();				
		processor.setLowercase(true);
		reader.setTokenizer(tokenizer);
		reader.setTokenProcessor(processor);

		
		WebDocumentReader reader2 = new WebDocumentReader(web_data_file);
		reader2.setTokenizer(tokenizer);
		reader2.setTokenProcessor(processor);
		Search system = new Search(reader, reader2, tokenizer, processor,
				Index.TERM_MODIFIER.l, Index.DOC_MODIFIER.t, Index.LENGTH_MODIFIER.c);

		system.run();
	}*/
	
	/**
	 * Use this constructor if you want to have document IDs
	 * 
	 * @param reader
	 * @param termModifier
	 * @param docModifier
	 * @param lengthModifier
	 */
	public Search(DocumentReader reader, Tokenizer tokenizer, TokenProcessor tokenProcessor,
			Index.TERM_MODIFIER termModifier, Index.DOC_MODIFIER docModifier, Index.LENGTH_MODIFIER lengthModifier){
		this.tokenizer = tokenizer;
		this.tokenProcessor = tokenProcessor;
		
		index = new Index(reader, termModifier, docModifier, lengthModifier);
	}
	
	/**
	 * Use this constructor if you want to have the actual document text returned
	 * 
	 * You should pass two instances of the reader, but that are of the same type
	 * and read from the same file.
	 * NOTE: For now, this is a bit of a hack, but it was the best I could think to do without
	 * making life too complicated
	 * 
	 * @param reader
	 * @param reader2
	 * @param termModifier
	 * @param docModifier
	 * @param lengthModifier
	 */
	public Search(DocumentReader reader, DocumentReader reader2,
			Tokenizer tokenizer, TokenProcessor tokenProcessor,
			Index.TERM_MODIFIER termModifier, Index.DOC_MODIFIER docModifier, Index.LENGTH_MODIFIER lengthModifier){
		this.tokenizer = tokenizer;
		this.tokenProcessor = tokenProcessor;
		
		index = new Index(reader, termModifier, docModifier, lengthModifier);
		
		id2doc = new Hashtable<Integer,Document>();
		
		while( reader2.hasNext() ){
			Document next = reader2.next();
			id2doc.put(next.getDocID(), next);
		}
	}
	
	/**
	 * Use this constructor if you want to have the actual document text returned
	 * 
	 * You should pass two instances of the reader, but that are of the same type
	 * and read from the same file.
	 * NOTE: For now, this is a bit of a hack, but it was the best I could think to do without
	 * making life too complicated
	 * 
	 * @param reader
	 * @param reader2
	 * @param termModifier
	 * @param docModifier
	 * @param lengthModifier
	 */
	public Search(WebDocumentReader reader, WebDocumentReader reader2,
			Tokenizer tokenizer, TokenProcessor tokenProcessor,
			Index.TERM_MODIFIER termModifier, Index.DOC_MODIFIER docModifier, Index.LENGTH_MODIFIER lengthModifier){
		this.tokenizer = tokenizer;
		this.tokenProcessor = tokenProcessor;
		
		index = new Index(reader, termModifier, docModifier, lengthModifier);
		id2web = new Hashtable<Integer,WebResultData>();
		
		while( reader2.hasNext() ){
			WebDocument next = (WebDocument)reader2.next();
			id2web.put(next.getDocID(), new WebResultData(next.getTitle(), next.getUrl(), next.getText()));
		}
	}
			
	/**
	 * Run the query interaction system.  The system
	 * waits for a query to be issued and then prints out the result.
	 * 
	 * If the user types "exit" the program ends, otherwise it will continue
	 * to accept queries.
	 * 
	 * Notice that both boolean and ranked queries can be issued.
	 */
	public void run(){
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		try{
			String textQuery = in.readLine();
					
			while( !textQuery.equals("exit") ){
				QueryResult result;
				
				if( textQuery.contains("!") ||
					textQuery.contains("AND") ||
					textQuery.contains("OR") ){
					result = index.booleanQuery(textQuery);
				}else{
					result = index.rankedQuery(new Query(tokenProcessor.process(tokenizer.tokenize(textQuery))));
				}
				
				if( result.size() == 0 ){
					System.out.println("No results");
				}else{
					int[] ids = result.getIDs();
					double[] scores = result.getScores();
					
					for( int i = 0; i < scores.length; i++ ){
						if( id2doc != null ){
							System.out.println(id2doc.get(ids[i]));
						}else if( id2web != null ){
							WebResultData data = id2web.get(ids[i]);
							
							System.out.println(data.getTitle());
							System.out.println(data.getUrl());
							System.out.println();
						}
						else{
							System.out.print(ids[i] + ":" + scores[i] + " ");
						}
					}
				
					System.out.println();
				}
				
				
				textQuery = in.readLine();
			}
		}catch(IOException e){
			throw new RuntimeException(e.toString());
		}
	}
}