package application.service;
import org.apache.commons.lang3.StringUtils;

public class SmithWaterman {



	private static int process(char[] mSeqA, char[] mSeqB) {
		int[][] mD;

		// init
		mD = new int[mSeqA.length + 1][mSeqB.length + 1];
		for (int i = 0; i <= mSeqA.length; i++) {
			mD[i][0] = 0;
		}
		for (int j = 0; j <= mSeqB.length; j++) {
			mD[0][j] = 0;
		}

		// end init

		int max = 0;
		for (int i = 1; i <= mSeqA.length; i++) {
			for (int j = 1; j <= mSeqB.length; j++) {
				int scoreDiag = mD[i-1][j-1] + weight(i, j, mSeqA, mSeqB);
				int scoreLeft = mD[i][j-1] - 1;
				int scoreUp = mD[i-1][j] - 1;
				mD[i][j] = Math.max(Math.max(Math.max(scoreDiag, scoreLeft), scoreUp), 0);
				if (mD[i][j] > max) {
					max = mD[i][j];
				}
			}
		}


		return max;
	}


	private static int weight(int i, int j, char[] mSeqA, char[] mSeqB) {
		if (mSeqA[i - 1] == mSeqB[j - 1]) {
			return 2;
		} else {
			return -1;
		}
	}


	public static int process(String seqA, String seqB) {
		String lowerA = StringUtils.stripAccents(seqA.toLowerCase()).replaceAll("[+-]", " ");
		String lowerB = StringUtils.stripAccents(seqB.toLowerCase()).replaceAll("[+-]", " ");

		return process(lowerA.toCharArray(), lowerB.toCharArray());
	}

	public static int processToPercent(String seqA, String seqB) {
		int minLength = Math.min(seqB.length(), seqA.length());
		int maxScore = minLength * 2;
		int score = SmithWaterman.process(seqB, seqA);

		return (score * 100 / maxScore);
	}

	public static void main(String[] args) {
		SmithWaterman.processToPercent("100", "h");
	}

}