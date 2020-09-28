/*
 * Copyright (c) 2020. vipyinzhiwei <vipyinzhiwei@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wsg.up.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import coil.api.load
import com.github.chrisbanes.photoview.PhotoView
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer
import com.wsg.up.R
import com.wsg.up.entity.RecommendItem
import com.wsg.up.ext.gone
import com.wsg.up.ext.invisibleAlphaAnimation
import com.wsg.up.ext.visible
import com.wsg.up.ext.visibleAlphaAnimation
import com.wsg.up.ui.activity.ugc.UgcDetailActivity
import com.wsg.up.utils.EyeConstants

class UgcDetailAdapter(val activity: UgcDetailActivity, var dataList: List<RecommendItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount() = dataList.size

    override fun getItemViewType(position: Int): Int {
        val item = dataList[position]
        return when (item.type) {
            EyeConstants.STR_COMMUNITY_COLUMNS_CARD -> {
                if (item.data?.dataType == EyeConstants.STR_FOLLOW_CARD_DATA_TYPE) EyeConstants.FOLLOW_CARD_TYPE
                else EyeConstants.UN_KNOW
            }
            else -> EyeConstants.UN_KNOW
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        EyeConstants.FOLLOW_CARD_TYPE -> FollowCardViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_ugc_detail, parent, false))
        else -> EmptyViewHolder(View(parent.context).apply { layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT) })
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FollowCardViewHolder -> {
                val item = dataList[position]
                holder.run {
                    videoPlayer.gone()
                    viewPagerPhotos.gone()
                    flHeader.visible()
                    llUgcInfo.visible()

                    ivPullDown.setOnClickListener { activity.finish() }
                    ivAvatar.load(item.data?.content?.data?.owner?.avatar)
                    if (null != item.data?.content?.data?.owner?.expert) ivAvatarStar.visible() else ivAvatarStar.gone()
                    tvNickName.text = item.data?.content?.data?.owner?.nickname
                    tvDescription.text = item.data?.content?.data?.description
                    if (item.data?.content?.data?.description!!.isBlank()) tvDescription.gone() else tvDescription.visible()
                    tvTagName.text = item.data?.content?.data?.tags?.first()?.name
                    if (item.data?.content?.data?.tags.isNullOrEmpty()) tvTagName.gone() else tvTagName.visible()
                    tvCollectionCount.text = item.data?.content?.data?.consumption?.collectionCount.toString()
                    tvReplyCount.text = item.data?.content?.data?.consumption?.replyCount.toString()
//                    setOnClickListener(tvPrivateLetter, tvFollow, ivCollectionCount, tvCollectionCount, ivReply, tvReplyCount, ivFavorites, tvFavorites, ivShare) {
//                        when (this) {
//                            tvPrivateLetter, tvFollow, ivCollectionCount, tvCollectionCount, ivFavorites, tvFavorites -> LoginActivity.start(activity)
//                            ivShare -> showDialogShare(activity, getShareContent(item))
//                            ivReply, tvReplyCount -> R.string.currently_not_supported.showToast()
//                            else -> {
//                            }
//                        }
//                    }
                    itemView.setOnClickListener { switchHeaderAndUgcInfoVisibility() }
                }
                when (item.data?.content?.type) {
                    EyeConstants.STR_VIDEO_TYPE -> {
                        holder.videoPlayer.visible()
                        holder.videoPlayer.run {
                            val data = item.data?.content?.data
                            val cover = ImageView(activity)
                            cover.scaleType = ImageView.ScaleType.CENTER_CROP
                            cover.load(data?.cover?.detail)
                            cover.parent?.run { removeView(cover) }
                            setThumbImageView(cover)
                            setThumbPlay(true)
                            setIsTouchWiget(false)
                            setLooping(true)
                            setPlayTag(data?.id.toString())
                            setPlayPosition(position)
                            setVideoAllCallBack(object : GSYSampleCallBack() {
                                override fun onClickBlank(url: String?, vararg objects: Any?) {
                                    super.onClickBlank(url, *objects)
                                    holder.switchHeaderAndUgcInfoVisibility()
                                }
                            })
                            setUp(data?.playUrl, false, null)
                        }
                    }
                    EyeConstants.STR_UGC_PICTURE_TYPE -> {
                        holder.viewPagerPhotos.visible()
                        item.data?.content?.data?.urls?.run {
                            holder.viewPagerPhotos.orientation = ViewPager2.ORIENTATION_HORIZONTAL
                            holder.viewPagerPhotos.offscreenPageLimit = 1
                            holder.viewPagerPhotos.adapter = PhotosAdapter(item.data?.content?.data?.urls, holder)
                            if (item.data?.content?.data?.urls.size > 1) {
                                holder.tvPhotoCount.visible()
                                holder.tvPhotoCount.text = String.format(activity.getString(R.string.photo_count), 1, item.data?.content?.data?.urls.size)
                            } else {
                                holder.tvPhotoCount.gone()
                            }
                            holder.viewPagerPhotos.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                                override fun onPageSelected(position: Int) {
                                    super.onPageSelected(position)
                                    holder.tvPhotoCount.text = String.format(activity.getString(R.string.photo_count), position + 1, item.data?.content?.data?.urls.size)
                                }
                            })
                        }
                    }
                    else -> {

                    }
                }
            }
            else -> {
                holder.itemView.gone()
            }
        }
    }

    class FollowCardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val videoPlayer = view.findViewById<GSYVideoPlayer>(R.id.videoPlayer)
        val viewPagerPhotos = view.findViewById<ViewPager2>(R.id.viewPagerPhotos)
        val ivPullDown = view.findViewById<ImageView>(R.id.ivPullDown)
        val tvPhotoCount = view.findViewById<TextView>(R.id.tvPhotoCount)
        val ivAvatar = view.findViewById<ImageFilterView>(R.id.ivAvatar)
        val ivAvatarStar = view.findViewById<ImageFilterView>(R.id.ivAvatarStar)
        val tvNickName = view.findViewById<TextView>(R.id.tvNickName)
        val tvPrivateLetter = view.findViewById<TextView>(R.id.tvPrivateLetter)
        val tvFollow = view.findViewById<TextView>(R.id.tvFollow)
        val tvDescription = view.findViewById<TextView>(R.id.tvDescription)
        val tvTagName = view.findViewById<TextView>(R.id.tvTagName)
        val ivCollectionCount = view.findViewById<ImageView>(R.id.ivCollectionCount)
        val tvCollectionCount = view.findViewById<TextView>(R.id.tvCollectionCount)
        val ivReply = view.findViewById<ImageView>(R.id.ivReply)
        val tvReplyCount = view.findViewById<TextView>(R.id.tvReplyCount)
        val ivFavorites = view.findViewById<ImageView>(R.id.ivFavorites)
        val tvFavorites = view.findViewById<TextView>(R.id.tvFavorites)
        val ivShare = view.findViewById<ImageView>(R.id.ivShare)
        val flHeader = view.findViewById<FrameLayout>(R.id.flHeader)
        val llUgcInfo = view.findViewById<LinearLayout>(R.id.llUgcInfo)

        fun switchHeaderAndUgcInfoVisibility() {
            if (ivPullDown.visibility == View.VISIBLE) {
                ivPullDown.invisibleAlphaAnimation()
                llUgcInfo.invisibleAlphaAnimation()
            } else {
                ivPullDown.visibleAlphaAnimation()
                llUgcInfo.visibleAlphaAnimation()
            }
        }
    }

    private fun getShareContent(item: RecommendItem): String {
        item.data?.content?.data!!.run {
            val linkUrl = "https://www.eyepetizer.net/detail.html?vid=${id}&utm_campaign=routine&utm_medium=share&utm_source=others&uid=0&resourceType=${resourceType}"
            return "${owner?.nickname} 在${activity.getString(R.string.app_name)}发表了作品：\n「${description}」\n${linkUrl}"
        }
    }

    inner class PhotosAdapter(val dataList: List<String>, val ugcHolder: FollowCardViewHolder) : RecyclerView.Adapter<PhotosAdapter.ViewHolder>() {

        inner class ViewHolder(view: PhotoView) : RecyclerView.ViewHolder(view) {
            val photoView = view
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val photoView = PhotoView(parent.context)
            photoView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            return ViewHolder(photoView)
        }

        override fun getItemCount() = dataList.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.photoView.load(dataList[position])
            holder.photoView.setOnClickListener { ugcHolder.switchHeaderAndUgcInfoVisibility() }
        }
    }

    class EmptyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}