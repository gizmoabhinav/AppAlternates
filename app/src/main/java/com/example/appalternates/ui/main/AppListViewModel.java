package com.example.appalternates.ui.main;

import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
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

public class AppListViewModel extends ViewModel {

    public static class DetectedAppViewModel {
        String iconUri;
        String name;
        String id;
        ArrayList<AlternateAppViewModel> alternateApps;
        DetectedAppViewModel(String id, String appName, String uri){
            iconUri = uri;
            name = appName;
            this.id  = id;
            alternateApps = new ArrayList<>();
        }
        public void addApp (AlternateAppViewModel app) {
            alternateApps.add(app);
        }
    }

    public static class AlternateAppViewModel implements Parcelable {
        String iconUri;
        String name;
        String id;
        boolean isIndian;
        AlternateAppViewModel(String id, String appName, String uri, boolean isIndian){
            iconUri = uri;
            name = appName;
            this.id  = id;
            this.isIndian = isIndian;
        }

        protected AlternateAppViewModel(Parcel in) {
            name = in.readString();
            id = in.readString();
            iconUri = in.readString();
            isIndian = in.readByte() != 0;
        }

        public static final Creator<AlternateAppViewModel> CREATOR = new Creator<AlternateAppViewModel>() {
            @Override
            public AlternateAppViewModel createFromParcel(Parcel in) {
                return new AlternateAppViewModel(in);
            }

            @Override
            public AlternateAppViewModel[] newArray(int size) {
                return new AlternateAppViewModel[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(name);
            parcel.writeString(id);
            parcel.writeString(iconUri);
            parcel.writeByte(isIndian?(byte)1:(byte)0);
        }
    }

    public void fetchLatestList(List<DetectedAppViewModel> applist, AppListAdapter adapter, PackageManager pm) {
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

    static class LatestFetcher extends AsyncTask<Object, Object, List<DetectedAppViewModel>> {

        private String uri = "https://gizmoabhinav.github.io";
        AppListAdapter adapter;
        List<DetectedAppViewModel> applist;
        PackageManager pm;

        LatestFetcher(List<DetectedAppViewModel> applist, AppListAdapter adapter, PackageManager pm) {
            this.applist = applist;
            this.pm = pm;
            this.adapter = adapter;
        }

        @Override
        protected List<DetectedAppViewModel> doInBackground(Object... objects) {

            ArrayList<DetectedAppViewModel> list = new ArrayList<>();
            try {
                URL url = new URL(uri + "/apps.xml");
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

                        DetectedAppViewModel detectedApp = new DetectedAppViewModel((idList.item(0)).getNodeValue(),
                                (nameList.item(0)).getNodeValue(),
                                uri+(imageList.item(0)).getNodeValue());

                        NodeList indiaAlternate = fstElmnt.getElementsByTagName("alternate-india");

                        for (int j = 0; j < indiaAlternate.getLength(); j++) {
                            Node alternatenode = indiaAlternate.item(j);

                            Element element = (Element) alternatenode;

                            NodeList name = element.getElementsByTagName("name");
                            Element nameElement1 = (Element) name.item(0);
                            name = nameElement1.getChildNodes();

                            NodeList id = element.getElementsByTagName("id");
                            Element idelement = (Element) id.item(0);
                            id = idelement.getChildNodes();

                            NodeList image = element.getElementsByTagName("image");
                            Element img = (Element) image.item(0);
                            image = img.getChildNodes();

                            AlternateAppViewModel alternateApp = new AlternateAppViewModel((id.item(0)).getNodeValue(),
                                    (name.item(0)).getNodeValue(),
                                    uri+(image.item(0)).getNodeValue(),
                                    true);

                            detectedApp.addApp(alternateApp);
                        }

                        NodeList otherAlternate = fstElmnt.getElementsByTagName("alternate");

                        for (int j = 0; j < otherAlternate.getLength(); j++) {
                            Node alternatenode = otherAlternate.item(j);

                            Element element = (Element) alternatenode;

                            NodeList name = element.getElementsByTagName("name");
                            Element nameElement1 = (Element) name.item(0);
                            name = nameElement1.getChildNodes();

                            NodeList id = element.getElementsByTagName("id");
                            Element idelement = (Element) id.item(0);
                            id = idelement.getChildNodes();

                            NodeList image = element.getElementsByTagName("image");
                            Element img = (Element) image.item(0);
                            image = img.getChildNodes();

                            AlternateAppViewModel alternateApp = new AlternateAppViewModel((id.item(0)).getNodeValue(),
                                    (name.item(0)).getNodeValue(),
                                    uri+(image.item(0)).getNodeValue(),
                                    true);

                            detectedApp.addApp(alternateApp);
                        }


                        list.add(detectedApp);
                    }

                }
                return list;
            } catch (Exception e) {
                return new ArrayList<>();
            }
        }

        @Override
        protected void onPostExecute(List<DetectedAppViewModel> result) {
            applist.clear();
            applist.addAll(result);
            adapter.notifyItemRangeInserted(0, result.size());
        }
    }
}
