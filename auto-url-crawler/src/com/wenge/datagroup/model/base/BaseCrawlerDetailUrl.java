package com.wenge.datagroup.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class BaseCrawlerDetailUrl<M extends BaseCrawlerDetailUrl<M>> extends Model<M> implements IBean {

	public M setId(java.lang.Integer id) {
		set("id", id);
		return (M)this;
	}

	public java.lang.Integer getId() {
		return get("id");
	}

	public M setTitle(java.lang.String title) {
		set("title", title);
		return (M)this;
	}

	public java.lang.String getTitle() {
		return get("title");
	}

	public M setDetailUrl(java.lang.String detailUrl) {
		set("detail_url", detailUrl);
		return (M)this;
	}

	public java.lang.String getDetailUrl() {
		return get("detail_url");
	}

	public M setListUrlId(java.lang.Integer listUrlId) {
		set("list_url_id", listUrlId);
		return (M)this;
	}

	public java.lang.Integer getListUrlId() {
		return get("list_url_id");
	}

	public M setClusterId(java.lang.Integer clusterId) {
		set("cluster_id", clusterId);
		return (M)this;
	}

	public java.lang.Integer getClusterId() {
		return get("cluster_id");
	}

	public M setPageNum(java.lang.Integer pageNum) {
		set("page_num", pageNum);
		return (M)this;
	}

	public java.lang.Integer getPageNum() {
		return get("page_num");
	}

	public M setInsertTime(java.util.Date insertTime) {
		set("insert_time", insertTime);
		return (M)this;
	}

	public java.util.Date getInsertTime() {
		return get("insert_time");
	}

	public M setStatus(java.lang.Integer status) {
		set("status", status);
		return (M)this;
	}

	public java.lang.Integer getStatus() {
		return get("status");
	}

}