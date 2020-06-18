package com.falcon.switchapp.ui.main;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.lifecycle.ViewModel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class AppListViewModel extends ViewModel {

    public List<DetectedAppViewModel> getApplist() {
        return applist;
    }

    public interface IOnLoadCallback {
        void onLoad(List<DetectedAppViewModel> list);
    }

    public static class DetectedAppViewModel {
        String iconUri;
        Drawable drawable;
        String name;
        String id;
        String description;
        Country country;
        ArrayList<AlternateAppViewModel> alternateApps;
        DetectedAppViewModel(String id, String appName, String uri, String description){
            iconUri = uri;
            name = appName;
            this.id  = id;
            this.description = description;
            alternateApps = new ArrayList<>();
        }
        DetectedAppViewModel(String id, String appName, Drawable drawable, String description){
            this.drawable = drawable;
            name = appName;
            this.id  = id;
            this.description = description;
            alternateApps = new ArrayList<>();
        }
        public void addApp (AlternateAppViewModel app) {
            alternateApps.add(app);
        }
        public void setCountry (Country country) {
            this.country = country;
        }
    }

    public enum Country {
        All,
        China,
        India,
        USA
    }

    public static class AlternateAppViewModel implements Parcelable {
        String iconUri;
        String name;
        String id;
        String description;
        boolean isIndian;

        AlternateAppViewModel(String id, String appName, String uri, String description, boolean isIndian){
            iconUri = uri;
            name = appName;
            this.id  = id;
            this.description = description;
            this.isIndian = isIndian;
        }

        protected AlternateAppViewModel(Parcel in) {
            name = in.readString();
            id = in.readString();
            description = in.readString();
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
            parcel.writeString(description);
            parcel.writeString(iconUri);
            parcel.writeByte(isIndian?(byte)1:(byte)0);
        }
    }



    private List<DetectedAppViewModel> applist = new ArrayList<>();
    private List<DetectedAppViewModel> chineseApplist = new ArrayList<>();
    private List<DetectedAppViewModel> indianApplist = new ArrayList<>();

    public void fetchLatestList(PackageManager pm, IOnLoadCallback callback, Country country) {
        switch (country) {
            case India:
                if (indianApplist.isEmpty()) {
                    AppListFetcher fetcher = new AppListFetcher(applist, chineseApplist, indianApplist, pm, callback);
                    fetcher.execute();
                } else {
                    callback.onLoad(indianApplist);
                }
                break;
            case China:
                if (chineseApplist.isEmpty()) {
                    AppListFetcher fetcher = new AppListFetcher(applist, chineseApplist, indianApplist, pm, callback);
                    fetcher.execute();
                } else {
                    callback.onLoad(chineseApplist);
                }
                break;
            case All:
            default:
                if (applist.isEmpty()) {
                    AppListFetcher fetcher = new AppListFetcher(applist, chineseApplist, indianApplist, pm, callback);
                    fetcher.execute();
                } else {
                    callback.onLoad(applist);
                }
        }
    }

    static class AppListFetcher extends AsyncTask<Object, Object, List<DetectedAppViewModel>> {

        List<DetectedAppViewModel> applist;
        List<DetectedAppViewModel> chineseApps;
        List<DetectedAppViewModel> indianApps;
        PackageManager pm;
        IOnLoadCallback callback;

        AppListFetcher(List<DetectedAppViewModel> applist, List<DetectedAppViewModel> chineseApps, List<DetectedAppViewModel> indianApps, PackageManager pm, IOnLoadCallback callback) {
            this.pm = pm;
            this.callback = callback;
            this.applist = applist;
            this.chineseApps = chineseApps;
            this.indianApps = indianApps;
        }

        @Override
        protected List<DetectedAppViewModel> doInBackground(Object... objects) {
            List<PackageInfo> packageList = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS);
            chineseApps.clear();
            indianApps.clear();

            HashMap<String, DetectedAppViewModel> map = getChineseApps();

            ArrayList<DetectedAppViewModel> list = new ArrayList<>();
            ArrayList<DetectedAppViewModel> nonChineseApps = new ArrayList<>();

            /*To filter out System apps*/
            for(PackageInfo pi : packageList) {
                boolean b = isSystemPackage(pi);
                if(!b) {
                    DetectedAppViewModel detectedApp = new DetectedAppViewModel(pi.packageName,
                            pi.applicationInfo.loadLabel(pm).toString(),
                            pi.applicationInfo.loadIcon(pm),
                            "");
                    if (map.containsKey(pi.packageName)) {
                        detectedApp.country = Country.China;
                        detectedApp.description = map.get(pi.packageName).description;
                        detectedApp.alternateApps = map.get(pi.packageName).alternateApps;
                        chineseApps.add(detectedApp);
                    } else {
                        nonChineseApps.add(detectedApp);
                    }
                }
            }

            list.addAll(chineseApps);
            list.addAll(nonChineseApps);
            return list;
        }

        private boolean isSystemPackage(PackageInfo pkgInfo) {
            return ((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) ? true
                    : false;
        }

        @Override
        protected void onPostExecute(List<DetectedAppViewModel> result) {
            callback.onLoad(result);
        }
    }

    private static HashMap<String, DetectedAppViewModel> getChineseApps () {
        String uri = "https://gizmoabhinav.github.io";
        HashMap<String, DetectedAppViewModel> list = new HashMap<>();
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

                NodeList descriptionList = fstElmnt.getElementsByTagName("description");
                String desc = "";
                if (descriptionList.getLength()>0) {
                    Element descElement = (Element) descriptionList.item(0);
                    descriptionList = descElement.getChildNodes();
                    desc = (descriptionList.item(0)).getNodeValue();
                }

                DetectedAppViewModel detectedApp = new DetectedAppViewModel((idList.item(0)).getNodeValue(),
                        (nameList.item(0)).getNodeValue(),
                        uri + (imageList.item(0)).getNodeValue(),
                        desc);

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

                    NodeList descList = element.getElementsByTagName("description");
                    String desc1 = "";
                    if (descList.getLength() > 0) {
                        Element descElement = (Element) descList.item(0);
                        descList = descElement.getChildNodes();
                        desc1 = (descList.item(0)).getNodeValue();
                    }

                    AlternateAppViewModel alternateApp = new AlternateAppViewModel((id.item(0)).getNodeValue(),
                            (name.item(0)).getNodeValue(),
                            uri + (image.item(0)).getNodeValue(),
                            desc1,
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

                    NodeList descList = element.getElementsByTagName("description");
                    String desc1 = "";
                    if (descriptionList.getLength() > 0) {
                        Element descElement = (Element) descList.item(0);
                        descList = descElement.getChildNodes();
                        desc1 = (descList.item(0)).getNodeValue();
                    }

                    AlternateAppViewModel alternateApp = new AlternateAppViewModel((id.item(0)).getNodeValue(),
                            (name.item(0)).getNodeValue(),
                            uri + (image.item(0)).getNodeValue(),
                            desc1,
                            true);

                    detectedApp.addApp(alternateApp);
                }


                list.put(detectedApp.id, detectedApp);


            }
            return list;
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
}
