package lily.tool.stablemarriage;

// StableMarriage.java
// Kenneth J. Goldman
// April, 2001

/* This is the completed code for the example done in class.
 * The additional code is concerned with creating the Person objects,
 * setting up the rankings, and printing.
 * You can change the number of comples by changing the constant.
 * Rankings are created randomly.
 * At each step in the algorithm, the updated social register is printed.
 */

import java.util.*;

public class StableMarriage {
	public static int NUMBER_OF_Men = 3;

	public static int NUMBER_OF_Women = 3;

	public LinkedList[] MenPre = new LinkedList[NUMBER_OF_Men];

	public LinkedList[] WomenPre = new LinkedList[NUMBER_OF_Women];

	public SocialRegister sr;

	public static void main(String[] args) {
		(new StableMarriage()).run();
	}

	public void setnumber(int Mnum, int Wnum) {
		NUMBER_OF_Men = Mnum;
		NUMBER_OF_Women = Wnum;
	}

	public void setpreferences(LinkedList[] MPre, LinkedList[] WPre) {
		MenPre = MPre;
		WomenPre = WPre;
	}

	public int[] getresult() {
		String result = "";
		String str1, str2;
		int[] map = new int[NUMBER_OF_Men];
		Set couples = sr.engagements.entrySet(); // get the set of couples
		Iterator it = couples.iterator();
		while (it.hasNext()) {
			Map.Entry couple = (Map.Entry) it.next();
			result += "   (" + couple.getKey() + "," + couple.getValue()
					+ ")\n";

			// 取Man
			str1 = couple.getValue().toString();
			// 取Woman
			str2 = couple.getKey().toString();
			int i = Integer.parseInt(str1.substring(1, str1.length()));
			int j = Integer.parseInt(str2.substring(1, str2.length()));
			map[i - 1] = j - 1;// 变为0--n来记录
		}
		return map;
	}

	public void run() {
		LinkedList eligibleMen = createEligibleMen(NUMBER_OF_Men);
		LinkedList eligibleWomen = createEligibleWomen(NUMBER_OF_Women);
		createMenPreferences(eligibleMen, eligibleWomen);
		createWomenPreferences(eligibleWomen, eligibleMen);
		// createPreferences(eligibleMen,eligibleWomen);
		// createPreferences(eligibleWomen,eligibleMen);
		sr = new SocialRegister(eligibleMen, eligibleWomen);
		// System.out.println(sr);
		while (sr.eligibleMenExist()) {
			Man tman = sr.getFirstEligible();
			tman.makeProposal();
			// System.out.println(sr);
		}
		// System.out.println(sr);
	}

	LinkedList createEligibleMen(int number) {
		LinkedList men = new LinkedList();
		for (int i = 1; i <= number; i++)
			men.add(new Man("M" + i));
		return men;
	}

	LinkedList createEligibleWomen(int number) {
		LinkedList women = new LinkedList();
		for (int i = 1; i <= number; i++)
			women.add(new Woman("W" + i));
		return women;
	}

	void createPreferencesRandom(LinkedList a, LinkedList b) {
		Iterator it = a.listIterator();
		while (it.hasNext()) {
			Person p = (Person) it.next();
			Rankings r = p.getRankings();
			r.addAll(b);
			// jumble randomly:
			int n = r.size();
			for (int i = 0; i < 2 * n; i++) {
				int loc = (int) (Math.random() * n); // choose a random
														// element
				r.add(r.remove(loc)); // put it at the back
			}
		}
	}

	void createPreferences(LinkedList a, LinkedList b) {
		Iterator it = a.listIterator();
		while (it.hasNext()) {
			Person p = (Person) it.next();
			Rankings r = p.getRankings();
			r.addAll(b);
			// jumble randomly:
			int n = r.size();
			for (int i = 0; i < 2 * n; i++) {
				int loc = (int) (Math.random() * n); // choose a random
														// element
				r.add(r.remove(loc)); // put it at the back
			}
		}
	}

	void createMenPreferences(LinkedList men, LinkedList women) {
		Iterator it = men.listIterator();
		int i = 0;
		while (it.hasNext()) {
			Person p = (Person) it.next();
			Rankings r = p.getRankings();// 取得preference的list
			// 加入对应的list
			for (Iterator it2 = MenPre[i].listIterator(); it2.hasNext();) {
				Integer hold = (Integer) it2.next();
				hold = new Integer(hold.intValue() + 1);
				String str = "W" + hold.toString();
				for (Iterator it3 = women.listIterator(); it3.hasNext();) {
					Woman tw = (Woman) it3.next();
					if (str.equals(tw.toString())) {
						r.add(tw);
						break;
					}
				}
			}
			i++;
		}
	}

	void createWomenPreferences(LinkedList women, LinkedList men) {
		Iterator it = women.listIterator();
		int i = 0;
		while (it.hasNext()) {
			Person p = (Person) it.next();
			Rankings r = p.getRankings();// 取得preference的list
			// 加入对应的list
			for (Iterator it2 = WomenPre[i].listIterator(); it2.hasNext();) {
				Integer hold = (Integer) it2.next();
				hold = new Integer(hold.intValue() + 1);
				String str = "M" + hold.toString();
				for (Iterator it3 = men.listIterator(); it3.hasNext();) {
					Man tm = (Man) it3.next();
					if (str.equals(tm.toString())) {
						r.add(tm);
						break;
					}
				}
			}
			i++;
		}
	}

}

class SocialRegister {
	public static SocialRegister defaultRegister; // singleton pattern

	LinkedList eligibleMen, women;

	HashMap engagements; // maps Women to Men

	SocialRegister(LinkedList eligibleMen, LinkedList eligibleWomen) {
		defaultRegister = this;
		this.eligibleMen = eligibleMen;
		women = eligibleWomen;
		engagements = new HashMap();
	}

	boolean eligibleMenExist() {
		return !eligibleMen.isEmpty();
	}

	Man getFirstEligible() {
		return (Man) eligibleMen.get(0);
	}

	void createEngagement(Woman w, Man m) {
		if (engagements.containsKey(w))
			eligibleMen.add(engagements.get(w));
		engagements.put(w, m);
		eligibleMen.remove(m);
	}

	public String toString() {
		String result = "";
		if (eligibleMenExist()) {
			result += "\n ELIGIBLE MEN: \n" + showList(eligibleMen);
			result += " WOMEN: \n" + showList(women);
			result += " ENGAGEMENTS: \n";
		} else {
			result += " MARRIAGES: \n";
		}
		result += showEngagements();
		return result;
	}

	String showList(LinkedList eligible) {
		String result = "";
		Iterator it = eligible.listIterator();
		while (it.hasNext()) {
			Person p = (Person) it.next();
			result += "    " + p + ":" + p.getRankings() + "\n";
		}
		return result;
	}

	String showEngagements() {
		String result = "";
		Set couples = engagements.entrySet(); // get the set of couples
		Iterator it = couples.iterator();
		while (it.hasNext()) {
			Map.Entry couple = (Map.Entry) it.next();
			result += "   (" + couple.getKey() + "," + couple.getValue()
					+ ")\n";
		}
		return result;
	}
}

class Rankings extends LinkedList {
	public void trim(Object x) {
		// removes x and all elements after it
		Iterator it = listIterator();
		boolean found = false;
		while (!found)
			if (it.next() == x) {
				found = true;
				it.remove();
			}
		while (it.hasNext()) {
			it.next();
			it.remove();
		}
	}

	public String toString() {
		String result = "";
		Iterator it = listIterator();
		while (it.hasNext())
			result += " " + it.next();
		return result;
	}
}

class Person {
	String name;

	Rankings preferences;

	Person(String name) {
		this.name = name;
		preferences = new Rankings();
	}

	Rankings getRankings() {
		return preferences;
	}

	public String toString() {
		return name;
	}
}

class Man extends Person {
	Man(String name) {
		super(name);
	}

	void makeProposal() {
		Woman twm = (Woman) this.preferences.removeFirst();
		twm.considerProposal(this);
	}
}

class Woman extends Person {
	Woman(String name) {
		super(name);
	}

	void considerProposal(Man m) {
		if (this.preferences.contains(m)) {
			SocialRegister.defaultRegister.createEngagement(this, m);
			preferences.trim(m);
		}
	}
}
