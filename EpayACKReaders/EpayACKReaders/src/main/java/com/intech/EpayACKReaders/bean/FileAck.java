package com.intech.EpayACKReaders.bean;

import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "fileAckDetails" })
@XmlRootElement(name = "FileAck")
public class FileAck {

	@XmlElement(name = "FileAckDetails", required = true)
	protected FileAck.FileAckDetails fileAckDetails;
	@XmlAttribute(name = "MsgId", required = true)
	protected String msgId;
	@XmlAttribute(name = "MsgCrDtTm", required = true)
	@XmlSchemaType(name = "dateTime")
	protected XMLGregorianCalendar msgCrDtTm;
	@XmlAttribute(name = "Src", required = true)
	protected String src;
	@XmlAttribute(name = "Dest", required = true)
	@XmlSchemaType(name = "unsignedShort")
	protected String dest;
	@XmlAttribute(name = "BankCode", required = true)
	@XmlSchemaType(name = "unsignedShort")
	protected String bankCode;
	@XmlAttribute(name = "BankName", required = true)
	protected String bankName;
	@XmlAttribute(name = "NbOfTxs", required = true)
	@XmlSchemaType(name = "unsignedByte")
	protected String nbOfTxs;

	public FileAck.FileAckDetails getFileAckDetails() {
		return fileAckDetails;
	}

	public void setFileAckDetails(FileAck.FileAckDetails value) {
		this.fileAckDetails = value;
	}

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String value) {
		this.msgId = value;
	}

	public XMLGregorianCalendar getMsgCrDtTm() {
		return msgCrDtTm;
	}

	public void setMsgCrDtTm(XMLGregorianCalendar value) {
		this.msgCrDtTm = value;
	}

	public String getSrc() {
		return src;
	}

	public void setSrc(String value) {
		this.src = value;
	}

	public String getDest() {
		return dest;
	}

	public void setDest(String value) {
		this.dest = value;
	}

	public String getBankCode() {
		return bankCode;
	}

	public void setBankCode(String value) {
		this.bankCode = value;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String value) {
		this.bankName = value;
	}

	public String getNbOfTxs() {
		return nbOfTxs;
	}

	public void setNbOfTxs(String value) {
		this.nbOfTxs = value;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = { "msgTp" })
	public static class FileAckDetails {

		@XmlElement(name = "MsgTp", required = true)
		protected FileAck.FileAckDetails.MsgTp msgTp;

		public FileAck.FileAckDetails.MsgTp getMsgTp() {
			return msgTp;
		}

		public void setMsgTp(FileAck.FileAckDetails.MsgTp value) {
			this.msgTp = value;
		}

		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlType(name = "", propOrder = { "data" })
		public static class MsgTp {

			@XmlElement(name = "Data", required = true)
			protected List<FileAck.FileAckDetails.MsgTp.Data> data;
			@XmlAttribute(name = "MsgNm", required = true)
			protected String msgNm;

			public List<FileAck.FileAckDetails.MsgTp.Data> getData() {
				if (data == null) {
					data = new ArrayList<FileAck.FileAckDetails.MsgTp.Data>();
				}
				return this.data;
			}

			public String getMsgNm() {
				return msgNm;
			}

			public void setMsgNm(String value) {
				this.msgNm = value;
			}

			@XmlAccessorType(XmlAccessType.FIELD)
			@XmlType(name = "", propOrder = { "infId" })
			public static class Data {

				@XmlElement(name = "InfId", required = true)
				protected FileAck.FileAckDetails.MsgTp.Data.InfId infId;
				@XmlAttribute(name = "MsgId", required = true)
				protected String msgId;
				@XmlAttribute(name = "FN", required = true)
				protected String fn;
				@XmlAttribute(name = "RecCnt", required = true)
				@XmlSchemaType(name = "unsignedByte")
				protected String recCnt;
				@XmlAttribute(name = "GpSts", required = true)
				protected String gpSts;
				@XmlAttribute(name = "ErrCd", required = true)
				protected String errCd;

				/**
				 * Gets the value of the infId property.
				 * 
				 * @return possible object is {@link FileAck.FileAckDetails.MsgTp.Data.InfId }
				 * 
				 */
				public FileAck.FileAckDetails.MsgTp.Data.InfId getInfId() {
					return infId;
				}

				public void setInfId(FileAck.FileAckDetails.MsgTp.Data.InfId value) {
					this.infId = value;
				}

				public String getMsgId() {
					return msgId;
				}

				public void setMsgId(String value) {
					this.msgId = value;
				}

				public String getFN() {
					return fn;
				}

				public void setFN(String value) {
					this.fn = value;
				}

				public String getRecCnt() {
					return recCnt;
				}

				public void setRecCnt(String value) {
					this.recCnt = value;
				}

				public String getGpSts() {
					return gpSts;
				}

				public void setGpSts(String value) {
					this.gpSts = value;
				}

				public String getErrCd() {
					return errCd;
				}

				public void setErrCd(String value) {
					this.errCd = value;
				}

				@XmlAccessorType(XmlAccessType.FIELD)
				@XmlType(name = "", propOrder = { "err" })
				public static class InfId {

					@XmlElement(name = "Err", required = true)
					protected FileAck.FileAckDetails.MsgTp.Data.InfId.Err err;
					@XmlAttribute(name = "Id", required = true)
					protected String id;
					@XmlAttribute(name = "ResCd", required = true)
					protected String resCd;
					@XmlAttribute(name = "ErrCd")
					protected String errCd;

					public FileAck.FileAckDetails.MsgTp.Data.InfId.Err getErr() {
						return err;
					}

					public void setErr(FileAck.FileAckDetails.MsgTp.Data.InfId.Err value) {
						this.err = value;
					}

					public String getId() {
						return id;
					}

					public void setId(String value) {
						this.id = value;
					}

					public String getResCd() {
						return resCd;
					}

					public void setResCd(String value) {
						this.resCd = value;
					}

					public String getErrCd() {
						return errCd;
					}

					public void setErrCd(String value) {
						this.errCd = value;
					}

					@XmlAccessorType(XmlAccessType.FIELD)
					@XmlType(name = "", propOrder = { "rcrdId", "errCd" })
					public static class Err {

						@XmlElement(name = "RcrdId")
						protected String rcrdId;
						@XmlElement(name = "ErrCd")
						protected String errCd;

						public String getRcrdId() {
							return rcrdId;
						}

						public void setRcrdId(String value) {
							this.rcrdId = value;
						}

						public String getErrCd() {
							return errCd;
						}

						public void setErrCd(String value) {
							this.errCd = value;
						}

					}

				}

			}

		}

	}

}
