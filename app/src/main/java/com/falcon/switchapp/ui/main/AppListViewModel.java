package com.falcon.switchapp.ui.main;

import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class AppListViewModel extends ViewModel {

    private static String APP_LIST_KEY = "APP_LIST_KEY";
    private static String DELIMITER = ",";
    private DatabaseReference mDatabase;

    public List<DetectedAppViewModel> getApplist() {
        return applist;
    }

    public interface IOnLoadCallback {
        void onLoad();
    }

    public static class DetectedAppViewModel {
        String iconUri;
        Drawable drawable;
        String name;
        String id;
        String description;
        Country country;
        String link;
        ArrayList<AlternateAppViewModel> alternateApps;
        DetectedAppViewModel(String id, String appName, String uri, String description){
            iconUri = uri;
            name = appName;
            this.id  = id;
            this.description = description;
            alternateApps = new ArrayList<>();
        }
        public DetectedAppViewModel(String id, String appName, String uri, String description, String link){
            this(id,appName,uri,description);
            this.link = link;
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

    public void fetchLatestList(PackageManager pm, SharedPreferences sharedPref, IOnLoadCallback callback) {
        AppListFetcher fetcher = new AppListFetcher(applist, pm, sharedPref, mDatabase, callback);
        fetcher.execute();
    }

    private List<DetectedAppViewModel> applist = new ArrayList<>();

    static class AppListFetcher extends AsyncTask<Object, Object, List<DetectedAppViewModel>> {

        List<DetectedAppViewModel> applist;
        PackageManager pm;
        IOnLoadCallback callback;
        SharedPreferences sharedPref;
        DatabaseReference dbRef;

        AppListFetcher(List<DetectedAppViewModel> applist, PackageManager pm, SharedPreferences sharedPref, DatabaseReference dbRef, IOnLoadCallback callback) {
            this.pm = pm;
            this.callback = callback;
            this.applist = applist;
            this.sharedPref = sharedPref;
            this.dbRef = dbRef;
        }

        @Override
        protected List<DetectedAppViewModel> doInBackground(Object... objects) {
            List<PackageInfo> packageList = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS);

            HashMap<String, DetectedAppViewModel> map = getChineseApps();

            HashSet<String> lastInstalledApps = getLastFetchedApps(sharedPref);
            HashSet<String> appIdList = new HashSet<>();

            ArrayList<DetectedAppViewModel> list = new ArrayList<>();
            ArrayList<DetectedAppViewModel> nonChineseApps = new ArrayList<>();
            ArrayList<DetectedAppViewModel> chineseApps = new ArrayList<>();

            boolean newAppsInstalled = false;
            boolean appsUninstalled = false;
            /*To filter out System apps*/
            for(PackageInfo pi : packageList) {
                boolean b = isSystemPackage(pi);
                if(!b) {
                    if (!lastInstalledApps.contains(pi.packageName)) {
                        newAppsInstalled = true;
                    }
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
                    appIdList.add(pi.packageName);
                }
            }

            list.addAll(chineseApps);
            list.addAll(nonChineseApps);

            if (list.size()<lastInstalledApps.size()) {
                appsUninstalled = true;
            } else if (list.size() == lastInstalledApps.size() && newAppsInstalled) {
                appsUninstalled = true;
            }

            if (appsUninstalled || newAppsInstalled) {
                uploadAppList(appIdList, dbRef);
                updateApps(sharedPref,appIdList);
            }

            return list;
        }

        private boolean isSystemPackage(PackageInfo pkgInfo) {
            return ((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) ? true
                    : false;
        }

        @Override
        protected void onPostExecute(List<DetectedAppViewModel> result) {
            applist.clear();
            applist.addAll(result);
            callback.onLoad();
        }
    }

    private static void uploadAppList(HashSet<String> appList, DatabaseReference postRef) {
        if (postRef == null) {
            postRef = FirebaseDatabase.getInstance().getReference("apps");
        }
        for (String app : appList) {
            postRef.child(app.replaceAll("\\.", "_")).runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    Long count = mutableData.getValue(Long.class);
                    if (count == null) {
                        count = 0L;
                    }
                    count++;
                    mutableData.setValue(count);
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(DatabaseError databaseError, boolean committed,
                                       DataSnapshot currentData) {
                    // Transaction completed
                }
            });
        }

    }

    private static HashSet<String> getLastFetchedApps (SharedPreferences sharedPref) {
        String apps[] = sharedPref.getString(APP_LIST_KEY,"").split(DELIMITER);
        if (apps == null || apps.length == 0 || apps.length == 1) {
            return new HashSet<>();
        }
        return new HashSet<String>(Arrays.asList(apps));
    }

    private static void updateApps (SharedPreferences sharedPref, HashSet<String> appList) {
        StringBuilder sb = new StringBuilder();
        for (String app : appList) {
            sb.append(app);
            sb.append(DELIMITER);
        }
        sb.deleteCharAt(sb.length()-1);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(APP_LIST_KEY, sb.toString());
        editor.commit();
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
