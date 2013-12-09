package org.wikapidia.pageview;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.wikapidia.core.WikapidiaException;
import org.wikapidia.core.dao.DaoException;
import org.wikapidia.core.dao.live.LocalPageLiveDao;
import org.wikapidia.core.lang.Language;
import org.wikapidia.core.model.Title;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.zip.GZIPInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: derian
 * Date: 12/1/13
 * Time: 5:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class PageViewIterator implements Iterator {

    private DateTime currentDate;
    private DateTime endDate;
    private Language lang;
    private File tempFolder;
    private static String BASE_URL = "http://dumps.wikimedia.your.org/other/pagecounts-raw/";
    private PageViewDataStruct nextData;

    public PageViewIterator(Language lang, String tempFolderName, int startYear, int startMonth, int startDay, int startHour,
                            int endYear, int endMonth, int endDay, int endHour) throws WikapidiaException, DaoException {
        this.lang = lang;
        this.tempFolder = new File(tempFolderName);
        if (!tempFolder.exists()){
            tempFolder.mkdir();
        }
        this.currentDate = new DateTime(startYear, startMonth, startDay, startHour, 0);
        if (currentDate.getMillis() < (new DateTime(2007, 12, 9, 18, 0)).getMillis()) {
            throw new WikapidiaException("No page view data supported before 6 PM on 12/09/2007");
        }
        this.endDate = new DateTime(endYear, endMonth, endDay, endHour, 0);
        nextData = getPageViewData();
    }

    public void remove() {
        throw new UnsupportedOperationException("Remove not supported for PageViewIterator");
    }

    public PageViewDataStruct next() {
        if (nextData == null) {
            throw new NoSuchElementException();
        }
        PageViewDataStruct currentData = nextData;

        try {
            nextData = getPageViewData();
        }
        catch (WikapidiaException wE) {
            wE.printStackTrace();
        }
        catch (DaoException dE) {
            dE.printStackTrace();
        }

        return currentData;
    }

    public boolean hasNext() {
         return (nextData != null);
    }

    private PageViewDataStruct getPageViewData() throws WikapidiaException, DaoException {
        if (currentDate.getMillis() > endDate.getMillis()) {
            return null;
        }

        // build up the file name for the page view data file from the current date
        String yearString = ((Integer) currentDate.getYear()).toString();
        String monthString = twoDigIntStr(currentDate.getMonthOfYear());
        String dayString = twoDigIntStr(currentDate.getDayOfMonth());
        String hourString = twoDigIntStr(currentDate.getHourOfDay());
        String fileName = "pagecounts-" + yearString + monthString + dayString + "-" + hourString;
        String fileNameSuffix = ".gz";

        String homeFolder = BASE_URL + String.format("%s/%s-%s/", yearString, yearString, monthString);
        File pageViewDataFile = null;
        int minutes = 0;
        while (pageViewDataFile == null && minutes < 60) {
            int seconds = 0;
            while (pageViewDataFile == null && seconds < 60) {
                String minutesString = twoDigIntStr(minutes);
                String secondsString = twoDigIntStr(seconds);
                fileName += minutesString + secondsString + fileNameSuffix;
                pageViewDataFile = downloadFile(homeFolder, fileName, tempFolder);
            }
        }

        TIntIntMap pageViewCounts = parsePageViewDataFromFile(lang, pageViewDataFile);
        DateTime nextDate = currentDate.plusHours(1);
        PageViewDataStruct pageViewData = new PageViewDataStruct(lang, currentDate, nextDate, pageViewCounts);


        currentDate = nextDate;
        return pageViewData;
    }

    private static String twoDigIntStr(int time){
        String rVal = Integer.toString(time);
        if (time < 10){
            rVal = "0" + rVal;
        }
        return rVal;
    }

    private static File downloadFile(String folderUrl, String fileName, File localFolder){

        try{
            URL url = new URL(folderUrl + fileName);
            String localPath = localFolder.getAbsolutePath() + "/" + fileName;
            File dest = new File(localPath);
            FileUtils.copyURLToFile(url, dest, 60000, 60000);
            File ungzipDest = new File(localPath.split("\\.")[0] + ".txt");
            ungzip(dest,ungzipDest);
            return ungzipDest;
        }catch(IOException e){
            e.printStackTrace();
            return null;
        }

    }

    private static void ungzip(File inFile, File outFile) throws IOException{

        BufferedInputStream gbin = new BufferedInputStream(new GZIPInputStream(new FileInputStream(inFile)));
        FileUtils.copyInputStreamToFile(gbin, outFile);
        gbin.close();
    }

    private static TIntIntMap parsePageViewDataFromFile(Language lang, File f) throws WikapidiaException, DaoException {

        try{
            TIntIntMap data = new TIntIntHashMap();
            LocalPageLiveDao pdao = new LocalPageLiveDao();
            BufferedReader br =  new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
            String curLine;
            while ((curLine = br.readLine().trim()) != null){
                String[] cols = curLine.split(" ");
                    if (cols[0].equals(lang.getLangCode())){
                            try{
                                String title = URLDecoder.decode(cols[1], "UTF-8");
                                int pageId = pdao.getIdByTitle(new Title(title, lang));
                                int numPageViews = Integer.parseInt(cols[2]);
                                data.adjustOrPutValue(pageId, numPageViews, numPageViews);
                            }
                            catch(IllegalArgumentException e){
                                //log.error("Encoding error examining this line: " + curLine);
                                throw new WikapidiaException("Encoding error examining this line: " + curLine, e);
                            }
                        }
                }
            br.close();

            return data;
        }
        catch(IOException e){
            throw new WikapidiaException(e);
        }

    }

}
