package com.reconciliation.pojo;

/**
 * 对账单实体
 * @author Administrator
 *
 */
public class StatementAccount {

	private Integer pRecordId;        //平台停车场流水号
	private Integer parkId;          //停车场编号
	private String plateNum;         //车牌号
	private Double originalAmount;   //应付金额
	private Double recordAmount;     //实付金额
	private Double couponamount;     //优惠金额
	private String tradeDate;        //交易日期
	private String tradeTime;        //交易时间
	private String status;           //记账状态
	private String explain;          //状态说明
	
	
	public Integer getpRecordId() {
		return pRecordId;
	}
	public void setpRecordId(Integer pRecordId) {
		this.pRecordId = pRecordId;
	}
	public Integer getParkId() {
		return parkId;
	}
	public void setParkId(Integer parkId) {
		this.parkId = parkId;
	}
	public String getPlateNum() {
		return plateNum;
	}
	public void setPlateNum(String plateNum) {
		this.plateNum = plateNum;
	}
	public Double getOriginalAmount() {
		return originalAmount;
	}
	public void setOriginalAmount(Double originalAmount) {
		this.originalAmount = originalAmount;
	}
	public Double getRecordAmount() {
		return recordAmount;
	}
	public void setRecordAmount(Double recordAmount) {
		this.recordAmount = recordAmount;
	}
	public Double getCouponamount() {
		return couponamount;
	}
	public void setCouponamount(Double couponamount) {
		this.couponamount = couponamount;
	}
	public String getTradeDate() {
		return tradeDate;
	}
	public void setTradeDate(String tradeDate) {
		this.tradeDate = tradeDate;
	}
	public String getTradeTime() {
		return tradeTime;
	}
	public void setTradeTime(String tradeTime) {
		this.tradeTime = tradeTime;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getExplain() {
		return explain;
	}
	public void setExplain(String explain) {
		this.explain = explain;
	}
	@Override
	public String toString() {
		return "StatementAccount [pRecordId=" + pRecordId + ", parkId=" + parkId + ", plateNum=" + plateNum
				+ ", originalAmount=" + originalAmount + ", recordAmount=" + recordAmount + ", couponamount="
				+ couponamount + ", tradeDate=" + tradeDate + ", tradeTime=" + tradeTime + ", status=" + status
				+ ", explain=" + explain + "]";
	}
	
	
}
