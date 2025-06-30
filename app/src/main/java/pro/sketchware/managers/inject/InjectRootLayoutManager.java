package pro.sketchware.managers.inject;

import android.util.Pair;

import com.besome.sketch.beans.ViewBean;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import a.a.a.wq;
import pro.sketchware.tools.ViewBeanFactory;
import pro.sketchware.tools.ViewBeanParser;
import pro.sketchware.utility.FileUtil;

public class InjectRootLayoutManager {
    private static InjectRootLayoutManager instance;
    private final String path;
    private final Gson gson = new Gson();

    private InjectRootLayoutManager(String sc_id) {
        path = wq.b(sc_id) + "/view_root";
    }

    public static synchronized InjectRootLayoutManager getInstance(String sc_id) {
        if (instance == null) {
            instance = new InjectRootLayoutManager(sc_id);
        }
        return instance;
    }

    public static Root getDefaultRootLayout() {
        Map<String, String> attrs = new LinkedHashMap<>();
        attrs.put("android:layout_width", "match_parent");
        attrs.put("android:layout_height", "match_parent");
        attrs.put("android:orientation", "vertical");
        attrs.put("android:background", "#FFFFFF");
        return new Root("LinearLayout", attrs);
    }

    public static Root toRoot(Pair<String, Map<String, String>> root) {
        return new Root(root.first, root.second);
    }

    public void set(String name, Root layout) {
        Map<String, Root> data = get();
        if (data == null) {
            data = new LinkedHashMap<>();
        }
        data.put(name, layout);
        save(data);
    }

    public void removeLayout(String name) {
        Map<String, Root> data = get();
        if (data != null) {
            data.remove(name);
            save(data);
        }
    }

    private void save(Map<String, Root> data) {
        FileUtil.writeFile(path, gson.toJson(data));
    }

    public Root getLayoutByFileName(String name) {
        return get().getOrDefault(name, getDefaultRootLayout());
    }

    public Map<String, Root> get() {
        try {
            if (FileUtil.isExistFile(path)) {
                return gson.fromJson(
                        FileUtil.readFile(path),
                        new TypeToken<LinkedHashMap<String, Root>>() {
                        }.getType());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LinkedHashMap<>();
    }

    public ViewBean toBean(String name) {
        Root root = getLayoutByFileName(name);
        ViewBean viewBean = new ViewBean("root", ViewBeanParser.getViewTypeByClassName(root.className));
        viewBean.convert = root.className;
        new ViewBeanFactory(viewBean).applyAttributes(root.getAttributes());
        
        if (root.children != null) {
            for (ViewBean child : root.children) {
                viewBean.view.addSubView(child);
            }
        }
        
        return viewBean;
    }

    public static class Root {
        @SerializedName("class_name")
        private String className;

        @SerializedName("attributes")
        private Map<String, String> attrs;

        @SerializedName("children")
        private List<ViewBean> children;

        public Root() {
        }

        public Root(String className, Map<String, String> attrs) {
            this.className = className;
            this.attrs = attrs;
        }

        public String getClassName() {
            return className;
        }

        public Map<String, String> getAttributes() {
            return attrs;
        }

        public void addChild(ViewBean child) {
            if (children == null) {
                children = new ArrayList<>();
            }
            children.add(child);
        }
    }
}