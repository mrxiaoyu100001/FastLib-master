package com.aries.library.fast.demo.adapter;

import android.view.View;

import com.aries.library.fast.demo.R;
import com.aries.library.fast.demo.entity.SubjectsEntity;
import com.aries.library.fast.demo.helper.RadiusViewHelper;
import com.aries.library.fast.manager.GlideManager;
import com.aries.ui.view.radius.RadiusRelativeLayout;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.flyco.labelview.LabelView;

/**
 * Created: AriesHoo on 2017/8/23 17:01
 * Function: 豆瓣电影适配器
 * Desc:
 */
public class SubjectMovieAdapter extends BaseQuickAdapter<SubjectsEntity, BaseViewHolder> {

    boolean isShowTop = false;

    public SubjectMovieAdapter(boolean isShowTop) {
        super(R.layout.item_subject_movie);
        this.isShowTop = isShowTop;
    }

    @Override
    protected void convert(BaseViewHolder helper, SubjectsEntity item) {
        helper.setText(R.id.tv_titleMovie, item.title)
                .setText(R.id.tv_typeMovie, "题材:" + item.getGenres())
                .setText(R.id.tv_yearMovie, "年份:" + item.year)
                .setText(R.id.tv_directorMovie, "导演:" + item.getDirectors())
                .setText(R.id.tv_castMovie, "主演:" + item.getCasts());
        GlideManager.loadRoundImg(item.images.large, helper.getView(R.id.iv_coverMovie),2);
        LabelView labelView = helper.getView(R.id.lv_topMovie);
        labelView.setText("Top" + (helper.getLayoutPosition() + 1));
        labelView.setVisibility(isShowTop ? View.VISIBLE : View.GONE);
        RadiusViewHelper.getInstance().setRadiusViewAdapter(((RadiusRelativeLayout) helper.itemView).getDelegate());
    }
}
