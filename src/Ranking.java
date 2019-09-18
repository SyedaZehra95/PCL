import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Ranking {
	private ArrayList<Individual>[] ranking_;

	Ranking(Population union) {
		if (ActivityData.isAborted()) {
			return;
		}
		int[] dominateMe = new int[union.size()];
		List<Integer>[] iDominate = new List[union.size()];
		List<Integer>[] front = new List[union.size() + 1];
		int flagDominate;

		for (int i = 0; i < front.length; i++)
			front[i] = new LinkedList<Integer>();

		for (int p = 0; p < union.size(); p++) {
			iDominate[p] = new LinkedList<Integer>();
			dominateMe[p] = 0;
		}

		for (int p = 0; p < (union.size() - 1); p++) {
			for (int q = p + 1; q < union.size(); q++) {
				flagDominate = compare(union.getIndividual(p), union.getIndividual(q));

				if (flagDominate == -1) {
					iDominate[p].add(q);
					dominateMe[q]++;
				} else if (flagDominate == 1) {
					iDominate[q].add(p);
					dominateMe[p]++;
				}
			}
		}

		for (int p = 0; p < union.size(); p++) {
			if (dominateMe[p] == 0) {
				front[0].add(p);
				union.getIndividual(p).setRank(0);
			}
		}

		int i = 0;
		Iterator<Integer> it1, it2;
		while (front[i].size() != 0) {
			i++;
			it1 = front[i - 1].iterator();
			while (it1.hasNext()) {
				it2 = iDominate[it1.next()].iterator();
				while (it2.hasNext()) {
					int index = it2.next();
					dominateMe[index]--;
					if (dominateMe[index] == 0) {
						front[i].add(index);
						union.getIndividual(index).setRank(i);
					}
				}
			}
		}

		ranking_ = new ArrayList[i];
		for (int j = 0; j < i; j++) {
			ranking_[j] = new ArrayList<Individual>(front[j].size());
			it1 = front[j].iterator();
			while (it1.hasNext()) {
				ranking_[j].add(union.getIndividual(it1.next()));
			}
		}
	}

	private int compare(Individual chromosome1, Individual chromosome2) {
		int dominate1 = 0;
		int dominate2 = 0;
		int flag;
		double value1 = 0, value2 = 0;

		for (int i = 0; i < 3; i++) {
			value1 = chromosome1.getFitness()[i];
			value2 = chromosome2.getFitness()[i];

			if (value1 > value2) {
				flag = 1;
			} else if (value1 < value2) {
				flag = -1;
			} else {
				flag = 0;
			}
			if (flag == -1) {
				dominate1 = 1;
			}
			if (flag == 1) {
				dominate2 = 1;
			}
		}
		if (dominate1 == dominate2) {
			return 0; // No one dominate the other
		}
		if (dominate1 == 1) {
			return -1; // solution1 dominate
		}
		return 1; // solution2 dominate
	}

	public int numberOfFronts() {
		return ranking_.length;
	}

	public ArrayList<Individual> getSubfront(int rank) {
		if (ActivityData.isAborted()) {
			return null;
		}
		return ranking_[rank];

	}
}
