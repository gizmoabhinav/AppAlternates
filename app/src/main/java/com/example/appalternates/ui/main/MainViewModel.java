package com.example.appalternates.ui.main;

import android.content.pm.PackageManager;
import android.os.AsyncTask;

import androidx.lifecycle.ViewModel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MainViewModel extends ViewModel {


    public void fetchLatestList(List<String[]> applist, AppListAdapter adapter, PackageManager pm) {
        LatestFetcher fetcher = new LatestFetcher(applist, adapter, pm);
        fetcher.execute();
    }

    private static boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    static class LatestFetcher extends AsyncTask<Object, Object, List<String[]>> {

        private String uri = "https://gizmoabhinav.github.io";
        AppListAdapter adapter;
        List<String[]> applist;
        PackageManager pm;
        LatestFetcher(List<String[]> applist, AppListAdapter adapter, PackageManager pm) {
            this.applist = applist;
            this.pm = pm;
            this.adapter = adapter;
        }

        @Override
        protected List<String[]> doInBackground(Object... objects) {

            ArrayList<String[]> list = new ArrayList<>();
            try {
                URL url = new URL(uri+"/apps.xml");
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(new InputSource(url.openStream()));
                doc.getDocumentElement().normalize();

                NodeList nodeList = doc.getElementsByTagName("app");

                for (int i = 0; i < nodeList.getLength(); i++) {

                    Node node = nodeList.item(i);

                    Element fstElmnt = (Element) node;

                    NodeList nameList = fstElmnt.getElementsByTagName("name");
                    Element nameElement = (Element) nameList.item(0);
                    nameList = nameElement.getChildNodes();

                    NodeList idList = fstElmnt.getElementsByTagName("id");
                    Element websiteElement = (Element) idList.item(0);
                    idList = websiteElement.getChildNodes();

                    NodeList imageList = fstElmnt.getElementsByTagName("image");
                    Element imageElement = (Element) imageList.item(0);
                    imageList = imageElement.getChildNodes();

                    if (isPackageInstalled((idList.item(0)).getNodeValue(), pm)) {
                        list.add(new String[]{(nameList.item(0)).getNodeValue()+ " installed", uri+(imageList.item(0)).getNodeValue()});
                    } else {
                        list.add(new String[]{(nameList.item(0)).getNodeValue()+ " not installed", uri+(imageList.item(0)).getNodeValue()});
                    }

                }
                return list;
            } catch (Exception e) {
                return new ArrayList<>();
            }
        }

        @Override
        protected void onPostExecute(List<String[]> result) {
            applist.clear();
            applist.addAll(result);
            adapter.notifyItemRangeInserted(0,result.size());
        }
    }
}
