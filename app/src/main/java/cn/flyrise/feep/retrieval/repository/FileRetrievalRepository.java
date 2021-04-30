package cn.flyrise.feep.retrieval.repository;

import static cn.flyrise.feep.retrieval.vo.RetrievalType.TYPE_FILES;

import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.media.common.FileCategoryTable;
import cn.flyrise.feep.retrieval.bean.FileRetrieval;
import cn.flyrise.feep.retrieval.bean.Retrieval;
import cn.flyrise.feep.retrieval.protocol.DRFile;
import cn.flyrise.feep.retrieval.protocol.FileRetrievalResponse;
import cn.flyrise.feep.retrieval.protocol.RetrievalSearchRequest;
import cn.flyrise.feep.retrieval.vo.RetrievalResults;
import java.util.ArrayList;
import java.util.List;
import rx.Subscriber;

/**
 * @author ZYP
 * @since 2018-05-09 16:30
 */
public class FileRetrievalRepository extends RetrievalRepository {

	@Override public void search(Subscriber<? super RetrievalResults> subscriber, String keyword) {
		this.mKeyword = keyword;
		RetrievalSearchRequest request = RetrievalSearchRequest.searchFile(keyword);
		FEHttpClient.getInstance().post(request, new ResponseCallback<FileRetrievalResponse>() {
			@Override public void onCompleted(FileRetrievalResponse response) {
				List<FileRetrieval> fileRetrievals = new ArrayList<>();
				if (response != null && response.data != null && CommonUtil.nonEmptyList(response.data.results)) {
					fileRetrievals = new ArrayList<>();
					fileRetrievals.add((FileRetrieval) header("文件"));

					for (DRFile file : response.data.results) {
						fileRetrievals.add(createRetrieval(file));
					}

					if (response.data.maxCount >= 3) {
						fileRetrievals.add((FileRetrieval) footer("更多文件"));
					}
				}

				subscriber.onNext(new RetrievalResults.Builder()
						.retrievalType(getType())
						.retrievals(fileRetrievals)
						.create());
			}

			@Override public void onFailure(RepositoryException repositoryException) {
				FELog.e("Retrieval file failed. Error: " + repositoryException.exception().getMessage());
				subscriber.onNext(emptyResult());
			}
		});
	}

	private FileRetrieval createRetrieval(DRFile file) {
		FileRetrieval retrieval = new FileRetrieval();
		retrieval.viewType = Retrieval.VIEW_TYPE_CONTENT;
		retrieval.retrievalType = getType();
		retrieval.content = fontDeepen(file.title, mKeyword);
		retrieval.extra = fontDeepen("来自 " + file.remark, mKeyword);

		retrieval.businessId = file.id;
		retrieval.userId = file.userId;
		retrieval.username = file.username;

		retrieval.url = FEHttpClient.getInstance().getHost() + FEHttpClient.KNOWLEDGE_DOWNLOAD_PATH + file.id;
		retrieval.filename = file.remark.substring(file.remark.lastIndexOf("/") + 1);
		retrieval.iconRes = FileCategoryTable.getIcon(FileCategoryTable.getType(file.fileattr));
		return retrieval;
	}

	@Override protected int getType() {
		return TYPE_FILES;
	}

	@Override protected Retrieval newRetrieval() {
		return new FileRetrieval();
	}
}
