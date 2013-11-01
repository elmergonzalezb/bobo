package org.apache.lucene.index;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.browseengine.bobo.geosearch.index.IGeoIndexer;

/**
 * @author Geoff Cooney
 */
@RunWith(SpringJUnit4ClassRunner.class) 
@ContextConfiguration( { "/TEST-servlet.xml" }) 
@IfProfileValue(name = "test-suite", values = { "unit", "all" })
public class GeoDocConsumerPerThreadTest {
    private final Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);  
    }};
    
    private DocConsumerPerThread mockDocConsumerPerThread;
    
    private IGeoIndexer mockGeoIndexer;
    
    private GeoDocConsumerPerThread geoDocConsumerPerThread;

    private DocumentsWriter.DocWriter mockDocWriter;
    
    DocumentsWriterThreadState documentsWriterThreadState;
    DocumentsWriter documentsWriter;
    
    final int docID = 10; 
    Document document;
    
    Version matchVersion = Version.LUCENE_CURRENT;
    Analyzer analyzer = new StandardAnalyzer(matchVersion);
    Directory directory;
    IndexWriter writer;
    FieldInfos fieldInfos;
    BufferedDeletesStream bufferedDeletesStream;
    
    @Before
    public void setUp() throws IOException {
        mockDocConsumerPerThread = context.mock(DocConsumerPerThread.class);
        mockDocWriter = context.mock(DocumentsWriter.DocWriter.class);
        
        mockGeoIndexer = context.mock(IGeoIndexer.class);
        
        documentsWriter = buildDocumentsWriter();
        
        document = buildDocument();
        
        documentsWriterThreadState = new DocumentsWriterThreadState(documentsWriter);
        documentsWriterThreadState.docState.doc = document;
        documentsWriterThreadState.docState.docID = docID;
        
        geoDocConsumerPerThread = new GeoDocConsumerPerThread(mockDocConsumerPerThread, documentsWriterThreadState, mockGeoIndexer);
    }
    
    @After
    public void tearDown() {
        context.assertIsSatisfied();
    }
    
    private DocumentsWriter buildDocumentsWriter() throws IOException {
        analyzer = new StandardAnalyzer(matchVersion);
        IndexWriterConfig config = new IndexWriterConfig(matchVersion, analyzer);
        Directory directory = context.mock(Directory.class);
        writer = context.mock(IndexWriter.class);
        FieldInfos fieldInfos = new FieldInfos();
        BufferedDeletesStream bufferedDeletesStream = context.mock(BufferedDeletesStream.class);
        
        DocumentsWriter documentsWriter = new DocumentsWriter(config, directory, 
                writer, fieldInfos, bufferedDeletesStream);
        return documentsWriter;
    }
    
    private Document buildDocument() {
        Document document = new Document();
        document.add(new Field("text", "my text".getBytes()));
        document.add(new Field("text", "more text".getBytes()));
        document.add(new Field("title", "A good title".getBytes()));
        
        return document;
    }
    
    @Test
    public void testAbort() {
        context.checking(new Expectations() {
            {
                one(mockDocConsumerPerThread).abort();
                
                one(mockGeoIndexer).abort();
            }
        });
        
        geoDocConsumerPerThread.abort();
    }
}
