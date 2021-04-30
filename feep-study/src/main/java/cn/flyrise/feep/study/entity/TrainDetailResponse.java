package cn.flyrise.feep.study.entity;

import cn.flyrise.feep.core.network.request.ResponseContent;
import java.util.List;

public class TrainDetailResponse extends ResponseContent {


	/**
	 * data : {"theme":"测试多视频 和 文件打开","sDate":"2020-11-08 11:53:00.0","eDate":"2020-11-08 22:59:00.0","trainType":"全员培训","trainTeacher":"何芝欣","video":[{"ID":"68E1D0C1-F39F-7D7B-BBAC-D1BC79A5F56C","PATH":"\\video\\2020\\11\\08\\20201108114420906772533\\68E1D0C1-F39F-7D7B-BBAC-D1BC79A5F56C.mp4","REALNAME":"视频一","TYPE":"mp4"},{"ID":"C7B08A13-77A4-32E7-E95C-91549264017E","PATH":"\\video\\2020\\11\\08\\20201108114435626036458\\C7B08A13-77A4-32E7-E95C-91549264017E.MP4","REALNAME":"视频二","TYPE":"MP4"},{"ID":"16596125-2424-EC20-AE76-2BA14C3FCFA1","PATH":"\\video\\2020\\11\\08\\20201108114440642076416\\16596125-2424-EC20-AE76-2BA14C3FCFA1.mp4","REALNAME":"视频三","TYPE":"mp4"}],"file":[]}
	 */

	private DataBean data;

	public DataBean getData() {
		return data;
	}

	public void setData(DataBean data) {
		this.data = data;
	}

	public static class DataBean {
		/**
		 * theme : 测试多视频 和 文件打开
		 * sDate : 2020-11-08 11:53:00.0
		 * eDate : 2020-11-08 22:59:00.0
		 * trainType : 全员培训
		 * trainTeacher : 何芝欣
		 * video : [{"ID":"68E1D0C1-F39F-7D7B-BBAC-D1BC79A5F56C","PATH":"\\video\\2020\\11\\08\\20201108114420906772533\\68E1D0C1-F39F-7D7B-BBAC-D1BC79A5F56C.mp4","REALNAME":"视频一","TYPE":"mp4"},{"ID":"C7B08A13-77A4-32E7-E95C-91549264017E","PATH":"\\video\\2020\\11\\08\\20201108114435626036458\\C7B08A13-77A4-32E7-E95C-91549264017E.MP4","REALNAME":"视频二","TYPE":"MP4"},{"ID":"16596125-2424-EC20-AE76-2BA14C3FCFA1","PATH":"\\video\\2020\\11\\08\\20201108114440642076416\\16596125-2424-EC20-AE76-2BA14C3FCFA1.mp4","REALNAME":"视频三","TYPE":"mp4"}]
		 * file : []
		 */

		private String theme;
		private String sDate;
		private String eDate;
		private String trainType;
		private String trainTeacher;
		private List<DataBean.VideoBean> video;
		private List<?> file;
		private String serverId;
		private String master_key;
		private String tableName;
		private String place;
		private String trainingMethod;
		private String assMethod;

		public String getTrainingMethod() {
			return trainingMethod;
		}

		public void setTrainingMethod(String trainingMethod) {
			this.trainingMethod = trainingMethod;
		}

		public String getAssMethod() {
			return assMethod;
		}

		public void setAssMethod(String assMethod) {
			this.assMethod = assMethod;
		}

		public String getPlace() {
			return place;
		}

		public void setPlace(String place) {
			this.place = place;
		}

		public String getServerId() {
			return serverId;
		}

		public String getMaster_key() {
			return master_key;
		}

		public String getTableName() {
			return tableName;
		}

		public String getTheme() {
			return theme;
		}

		public void setTheme(String theme) {
			this.theme = theme;
		}

		public String getSDate() {
			return sDate;
		}

		public void setSDate(String sDate) {
			this.sDate = sDate;
		}

		public String getEDate() {
			return eDate;
		}

		public void setEDate(String eDate) {
			this.eDate = eDate;
		}

		public String getTrainType() {
			return trainType;
		}

		public void setTrainType(String trainType) {
			this.trainType = trainType;
		}

		public String getTrainTeacher() {
			return trainTeacher;
		}

		public void setTrainTeacher(String trainTeacher) {
			this.trainTeacher = trainTeacher;
		}

		public List<DataBean.VideoBean> getVideo() {
			return video;
		}

		public void setVideo(List<DataBean.VideoBean> video) {
			this.video = video;
		}

		public List<?> getFile() {
			return file;
		}

		public void setFile(List<?> file) {
			this.file = file;
		}

		public static class VideoBean {
			/**
			 * ID : 68E1D0C1-F39F-7D7B-BBAC-D1BC79A5F56C
			 * PATH : \video\2020\11\08\20201108114420906772533\68E1D0C1-F39F-7D7B-BBAC-D1BC79A5F56C.mp4
			 * REALNAME : 视频一
			 * TYPE : mp4
			 */

			private String ID;
			private String PATH;
			private String REALNAME;
			private String TYPE;

			public String getID() {
				return ID;
			}

			public void setID(String ID) {
				this.ID = ID;
			}

			public String getPATH() {
				return PATH;
			}

			public void setPATH(String PATH) {
				this.PATH = PATH;
			}

			public String getREALNAME() {
				return REALNAME;
			}

			public void setREALNAME(String REALNAME) {
				this.REALNAME = REALNAME;
			}

			public String getTYPE() {
				return TYPE;
			}

			public void setTYPE(String TYPE) {
				this.TYPE = TYPE;
			}
		}
	}
}
