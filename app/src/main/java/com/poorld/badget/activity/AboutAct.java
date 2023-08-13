package com.poorld.badget.activity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.Html;
import android.text.Spanned;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.drakeet.about.AbsAboutActivity;
import com.drakeet.about.Card;
import com.drakeet.about.Category;
import com.drakeet.about.Contributor;
import com.drakeet.about.License;
import com.poorld.badget.R;

import java.util.List;

import de.robv.android.xposed.BuildConfig;

public class AboutAct extends AbsAboutActivity {
    @Override
    protected void onCreateHeader(@NonNull ImageView icon, @NonNull TextView slogan, @NonNull TextView version) {
        icon.setImageResource(R.mipmap.icon_sword);
        slogan.setText("Badget");
        version.setText("v" + getVersionName());
    }

    @Override
    protected void onItemsCreated(@NonNull List<Object> items) {
        items.add(new Category("What is this?"));
        Spanned fromHtml = Html.fromHtml(getString(R.string.about_help), Html.FROM_HTML_MODE_LEGACY);
        items.add(new Card(fromHtml));

        items.add(new Category("Developers"));
        items.add(new Contributor(R.mipmap.avater, "poorld", "https://github.com/poorld", "https://github.com/poorld/badget"));

        items.add(new Category("参考&致谢"));
        items.add(new Contributor(R.mipmap.svengong, "svengong", "xcubebase", "https://github.com/svengong/xcubebase"));
        items.add(new Contributor(R.mipmap.dr_tsng, "Dr-TSNG", "Hide-My-Applist", "https://github.com/Dr-TSNG/Hide-My-Applist"));
        items.add(new Contributor(R.mipmap.see_flower_x, "SeeFlowerX", "https://github.com/SeeFlowerX", "https://github.com/SeeFlowerX"));

        items.add(new Category("Open Source Licenses"));
        items.add(new License("MultiType", "drakeet", License.APACHE_2, "https://github.com/drakeet/MultiType"));
        items.add(new License("about-page", "drakeet", License.APACHE_2, "https://github.com/drakeet/about-page"));
    }

    public String getVersionName() {
        PackageManager manager = getPackageManager();
        String code = "1.0.0";
        try {
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            code = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return code;
    }
}
