package cryodex.modules.starwarslcg;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import cryodex.CryodexController;
import cryodex.CryodexController.Modules;
import cryodex.Main;
import cryodex.Player;
import cryodex.modules.Module;
import cryodex.modules.Tournament;
import cryodex.xml.XMLObject;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class SWLCGTournament implements XMLObject, Tournament {

	public enum InitialSeedingEnum {
		RANDOM, BY_GROUP, IN_ORDER;
	}

	private final List<SWLCGRound> rounds;
	private List<SWLCGPlayer> players;
	private final InitialSeedingEnum seedingEnum;
	private final SWLCGTournamentGUI tournamentGUI;
	private String name;
	private boolean startAsSingleElimination = false;

	public SWLCGTournament(Element tournamentElement) {

		this.players = new ArrayList<>();
		this.rounds = new ArrayList<>();
		seedingEnum = InitialSeedingEnum.RANDOM;

		tournamentGUI = new SWLCGTournamentGUI(this);

		String playerIDs = tournamentElement.getStringFromChild("PLAYERS");

		Module m = Modules.getModuleByName(getModuleName());

		for (String s : playerIDs.split(",")) {
			Player p = CryodexController.getPlayerByID(s);

			if (p != null) {
				SWLCGPlayer xp = (SWLCGPlayer) p.getModuleInfoByModule(m);
				if (xp != null) {
					players.add(xp);
				}
			}
		}

		Element roundElement = tournamentElement.getChild("ROUNDS");

		for (Element e : roundElement.getChildren()) {
			rounds.add(new SWLCGRound(e, this));
		}

		name = tournamentElement.getStringFromChild("NAME");

		int counter = 1;
		for (SWLCGRound r : rounds) {
			if (r.isElimination()) {
				getTournamentGUI().getRoundTabbedPane()
						.addSingleEliminationTab(r.getMatches().size() * 2,
								r.getPanel());
			} else {
				getTournamentGUI().getRoundTabbedPane().addSwissTab(counter,
						r.getPanel());
				counter++;
			}

		}

		getTournamentGUI().getRankingTable().setPlayers(getAllSWLCGPlayers());
	}

	public SWLCGTournament(String name, List<SWLCGPlayer> players,
			InitialSeedingEnum seedingEnum, boolean isSingleElimination) {
		this.name = name;
		this.players = new ArrayList<>(players);
		this.rounds = new ArrayList<>();
		this.seedingEnum = seedingEnum;
		this.startAsSingleElimination = isSingleElimination;

		tournamentGUI = new SWLCGTournamentGUI(this);
	}

	public SWLCGRound getLatestRound() {
		if (rounds == null || rounds.isEmpty()) {
			return null;
		} else {
			return rounds.get(rounds.size() - 1);
		}
	}

	public int getRoundNumber(SWLCGRound round) {
		int count = 0;
		for (SWLCGRound r : rounds) {
			count++;
			if (r == round) {
				return count;
			}
		}

		return 0;
	}

	public SWLCGRound getRound(int i) {
		if (rounds == null) {
			return null;
		} else {
			return rounds.get(i);
		}
	}

	public SWLCGRound getSelectedRound() {
		if (rounds == null) {
			return null;
		} else {
			return getAllRounds().get(
					getTournamentGUI().getRoundTabbedPane().getSelectedIndex());
		}
	}

	public List<SWLCGRound> getAllRounds() {
		return rounds;
	}

	@Override
	public int getRoundCount() {
		if (rounds == null) {
			return 0;
		} else {
			return rounds.size();
		}
	}

	@Override
	public void setPlayers(List<Player> players) {
		List<SWLCGPlayer> xwPlayers = new ArrayList<>();

		for (Player p : players) {
			SWLCGPlayer xp = new SWLCGPlayer(p);
			xwPlayers.add(xp);
		}

		setSWLCGPlayer(xwPlayers);
	}

	@Override
	public List<Player> getPlayers() {
		List<Player> players = new ArrayList<Player>();

		for (SWLCGPlayer xp : getSWLCGPlayers()) {
			players.add(xp.getPlayer());
		}

		return players;
	}

	public List<SWLCGPlayer> getSWLCGPlayers() {
		return players;
	}

	public void setSWLCGPlayer(List<SWLCGPlayer> players) {
		this.players = players;
	}

	public Set<SWLCGPlayer> getAllSWLCGPlayers() {
		Set<SWLCGPlayer> allPlayers = new TreeSet<SWLCGPlayer>(new SWLCGComparator(this,
				SWLCGComparator.rankingCompare));

		for (SWLCGRound r : getAllRounds()) {
			for (SWLCGMatch m : r.getMatches()) {
				if (m.isBye()) {
					allPlayers.add(m.getPlayer1());
				} else {
					allPlayers.add(m.getPlayer1());
					if (m.getPlayer2() != null) {
						allPlayers.add(m.getPlayer2());
					}
				}
			}
		}

		allPlayers.addAll(players);

		return allPlayers;
	}

	@Override
	public Set<Player> getAllPlayers() {
		Set<Player> players = new TreeSet<Player>();

		for (SWLCGPlayer xp : getAllSWLCGPlayers()) {
			players.add(xp.getPlayer());
		}

		return players;
	}

	@Override
	public SWLCGTournamentGUI getTournamentGUI() {
		return tournamentGUI;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void updateVisualOptions() {
		if (CryodexController.isLoading == false) {
			for (SWLCGRound r : getAllRounds()) {
				r.getPanel().resetGamePanels(true);
			}
		}
	}

	@Override
	public boolean generateNextRound() {
		if (getLatestRound().isComplete() == false) {
			JOptionPane
					.showMessageDialog(Main.getInstance(),
							"Current round is not complete. Please complete all matches before continuing");
			return false;
		}

		if (getLatestRound().isValid() == false) {
			JOptionPane
					.showMessageDialog(
							Main.getInstance(),
							"At least one tournamnt result is not correct. Check if points are backwards or a result should be a modified win or tie.");
			return false;
		}

		if (getLatestRound().isElimination()) {
			if (getLatestRound().getMatches().size() == 1) {
				JOptionPane
						.showMessageDialog(Main.getInstance(),
								"Final tournament complete. No more rounds will be generated.");
				return false;
			}
			generateSingleEliminationMatches(getLatestRound().getMatches()
					.size());
		} else {
			generateRound(getAllRounds().size() + 1);
		}
		return true;
	}

	@Override
	public void cancelRound(int roundNumber) {
		if (rounds.size() >= roundNumber) {
			// If we are generating a past round. Clear all existing rounds that
			// will be erased.
			while (rounds.size() >= roundNumber) {
				int index = rounds.size() - 1;
				SWLCGRound roundToRemove = rounds.get(index);
				for (SWLCGMatch m : roundToRemove.getMatches()) {
					m.setBye(false);
					m.setPlayer1(null);
					m.setPlayer2(null);
					m.setGame1Result(null);
					m.setGame2Result(null);
				}
				rounds.remove(roundToRemove);

				getTournamentGUI().getRoundTabbedPane().remove(index);
			}
		}
	}

	@Override
	public void generateRound(int roundNumber) {

		// if trying to skip a round...stop it
		if (roundNumber > rounds.size() + 1) {
			throw new IllegalArgumentException();
		}

		cancelRound(roundNumber);

		List<SWLCGMatch> matches;
		if (roundNumber == 1) {

			matches = new ArrayList<SWLCGMatch>();
			List<SWLCGPlayer> tempList = new ArrayList<>();
			tempList.addAll(getSWLCGPlayers());

			List<SWLCGPlayer> firstRoundByePlayers = new ArrayList<>();
			for (SWLCGPlayer p : tempList) {
				if (p.isFirstRoundBye()) {
					firstRoundByePlayers.add(p);
				}
			}
			tempList.removeAll(firstRoundByePlayers);

			if (seedingEnum == InitialSeedingEnum.IN_ORDER) {

				while (tempList.isEmpty() == false) {
					SWLCGPlayer player1 = tempList.get(0);
					SWLCGPlayer player2 = null;
					tempList.remove(0);
					if (tempList.isEmpty() == false) {
						player2 = tempList.get(0);
						tempList.remove(0);
					}

					SWLCGMatch match = new SWLCGMatch(player1, player2);
					matches.add(match);
				}

			} else if (seedingEnum == InitialSeedingEnum.RANDOM) {
				Collections.shuffle(tempList);

				while (tempList.isEmpty() == false) {
					SWLCGPlayer player1 = tempList.get(0);
					SWLCGPlayer player2 = tempList.get(tempList.size() - 1);
					tempList.remove(player1);
					if (player1 == player2) {
						player2 = null;
					} else {
						tempList.remove(player2);
					}

					SWLCGMatch match = new SWLCGMatch(player1, player2);
					matches.add(match);
				}
			} else if (seedingEnum == InitialSeedingEnum.BY_GROUP) {
				Map<String, List<SWLCGPlayer>> playerMap = new HashMap<String, List<SWLCGPlayer>>();

				// Add players to map
				for (SWLCGPlayer p : tempList) {
					List<SWLCGPlayer> playerList = playerMap.get(p.getPlayer()
							.getGroupName());

					if (playerList == null) {
						playerList = new ArrayList<>();
						String groupName = p.getPlayer().getGroupName() == null ? ""
								: p.getPlayer().getGroupName();
						playerMap.put(groupName, playerList);
					}

					playerList.add(p);
				}

				// Shuffle up the lists
				List<String> seedValues = new ArrayList<>(playerMap.keySet());
				Collections.shuffle(seedValues);

				// Shuffle each group list
				for (List<SWLCGPlayer> list : playerMap.values()) {
					Collections.shuffle(list);
				}

				// /////////////
				// Add new algorythm here
				// /////////////

				SWLCGPlayer p1 = null;
				SWLCGPlayer p2 = null;
				while (seedValues.isEmpty() == false) {
					int i = 0;
					while (i < seedValues.size()) {
						if (p1 == null) {
							p1 = playerMap.get(seedValues.get(i)).get(0);
						} else {
							p2 = playerMap.get(seedValues.get(i)).get(0);
							matches.add(new SWLCGMatch(p1, p2));
							p1 = null;
							p2 = null;
						}

						playerMap.get(seedValues.get(i)).remove(0);

						if (playerMap.get(seedValues.get(i)).isEmpty()) {
							seedValues.remove(i);
						} else {
							i++;
						}
					}

					Collections.shuffle(seedValues);
				}
				if (p1 != null) {
					matches.add(new SWLCGMatch(p1, null));
				}
			}

			for (SWLCGPlayer p : firstRoundByePlayers) {
				matches.add(new SWLCGMatch(p, null));
			}

		} else {

			matches = getMatches(getSWLCGPlayers());
		}
		SWLCGRound r = new SWLCGRound(matches, this, roundNumber);
		rounds.add(r);
		if (roundNumber == 1
				&& startAsSingleElimination
				&& (matches.size() == 1 || matches.size() == 2
						|| matches.size() == 4 || matches.size() == 8
						|| matches.size() == 16 || matches.size() == 32)) {
			r.setElimination(true);
			getTournamentGUI().getRoundTabbedPane().addSingleEliminationTab(
					r.getMatches().size() * 2, r.getPanel());
		} else {
			getTournamentGUI().getRoundTabbedPane().addSwissTab(roundNumber,
					r.getPanel());
		}

		getTournamentGUI().getRankingTable().setPlayers(getAllSWLCGPlayers());
	}

	private List<SWLCGMatch> getMatches(List<SWLCGPlayer> userList) {
		List<SWLCGMatch> matches = new ArrayList<SWLCGMatch>();

		List<SWLCGPlayer> tempList = new ArrayList<SWLCGPlayer>();
		tempList.addAll(userList);
		Collections.sort(tempList, new SWLCGComparator(this,
				SWLCGComparator.pairingCompare));

		SWLCGMatch byeMatch = null;
		// Setup the bye match if necessary
		// The player to get the bye is the lowest ranked player who has not had
		// a bye yet or who has the fewest byes
		if (tempList.size() % 2 == 1) {
			SWLCGPlayer byeUser = null;
			int byUserCounter = 1;
			int minByes = 0;
			try {
				while (byeUser == null
						|| byeUser.getByes(this) > minByes
						|| (byeUser.getMatches(this) != null && byeUser
								.getMatches(this)
								.get(byeUser.getMatches(this).size() - 1)
								.isBye())) {
					if (byUserCounter > tempList.size()) {
						minByes++;
						byUserCounter = 1;
					}
					byeUser = tempList.get(tempList.size() - byUserCounter);

					byUserCounter++;

				}
			} catch (ArrayIndexOutOfBoundsException e) {
				byeUser = tempList.get(tempList.size() - 1);
			}
			byeMatch = new SWLCGMatch(byeUser, null);
			tempList.remove(byeUser);
		}

		matches = new SWLCGRandomMatchGeneration(this, tempList).generateMatches();

		if (SWLCGMatch.hasDuplicate(matches)) {
			JOptionPane
					.showMessageDialog(Main.getInstance(),
							"Unable to resolve duplicate matches. Please review for best course of action.");
		}

		// Add the bye match at the end
		if (byeMatch != null) {
			matches.add(byeMatch);
		}

		return matches;
	}

	@Override
	public void generateSingleEliminationMatches(int cutSize) {

		List<SWLCGMatch> matches = new ArrayList<SWLCGMatch>();

		if (getLatestRound().isElimination()) {
			matches = SWLCGEliminationGeneration.generateNextEliminationRound(this);
		} else {
		    matches =SWLCGEliminationGeneration.setupInitialBracket(this, cutSize);
		}

		SWLCGRound r = new SWLCGRound(matches, this, null);
		r.setElimination(true);
		rounds.add(r);
		getTournamentGUI().getRoundTabbedPane().addSingleEliminationTab(
				cutSize, r.getPanel());

		CryodexController.saveData();
	}

	@Override
	public StringBuilder appendXML(StringBuilder sb) {

		String playerString = "";
		String seperator = "";
		for (SWLCGPlayer p : players) {
			playerString += seperator + p.getPlayer().getSaveId();
			seperator = ",";
		}

		XMLUtils.appendObject(sb, "PLAYERS", playerString);

		XMLUtils.appendList(sb, "ROUNDS", "ROUND", getAllRounds());

		XMLUtils.appendObject(sb, "NAME", name);
		XMLUtils.appendObject(sb, "MODULE", Modules.SWLCG.getName());

		return sb;
	}

	@Override
	public void startTournament() {
		generateRound(1);
	}

	@Override
	public void addPlayer(Player p) {
		
		for(SWLCGRound r : getAllRounds()){
			for(SWLCGMatch m : r.getMatches()){
				if(m.getPlayer1().getPlayer().equals(p)){
					getSWLCGPlayers().add(m.getPlayer1());
					return;
				} else if(m.getPlayer2() != null && m.getPlayer2().getPlayer().equals(p)) {
					getSWLCGPlayers().add(m.getPlayer2());
					return;
				}
			}
		}
		
		SWLCGPlayer xPlayer = new SWLCGPlayer(p);
		getSWLCGPlayers().add(xPlayer);
	}

	@Override
	public void dropPlayer(Player p) {

		SWLCGPlayer xPlayer = null;

		for (SWLCGPlayer xp : getSWLCGPlayers()) {
			if (xp.getPlayer() == p) {
				xPlayer = xp;
				break;
			}
		}

		if (xPlayer != null) {
			getSWLCGPlayers().remove(xPlayer);
		}

		resetRankingTable();
	}

	@Override
	public void resetRankingTable() {
		getTournamentGUI().getRankingTable().setPlayers(getAllSWLCGPlayers());
	}

	@Override
	public Icon getIcon() {
		URL imgURL = SWLCGTournament.class.getResource("l.png");
		if (imgURL == null) {
			System.out.println("fail!!!!!!!!!!");
		}
		ImageIcon icon = new ImageIcon(imgURL);
		return icon;
	}

	@Override
	public String getModuleName() {
		return Modules.SWLCG.getName();
	}

    @Override
    public void massDropPlayers(int minScore, int maxCount) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void massDropPlayers(List<Player> playersToDrop) {
        // TODO Auto-generated method stub
        
    }
}
