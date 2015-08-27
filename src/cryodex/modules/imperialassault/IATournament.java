package cryodex.modules.imperialassault;

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

public class IATournament implements XMLObject, Tournament {

	public enum InitialSeedingEnum {
		RANDOM, BY_GROUP, IN_ORDER;
	}

	private final List<IARound> rounds;
	private List<IAPlayer> players;
	private final InitialSeedingEnum seedingEnum;
	private final IATournamentGUI tournamentGUI;
	private String name;
	private boolean startAsSingleElimination = false;

	public IATournament(Element tournamentElement) {

		this.players = new ArrayList<>();
		this.rounds = new ArrayList<>();
		seedingEnum = InitialSeedingEnum.RANDOM;

		tournamentGUI = new IATournamentGUI(this);

		String playerIDs = tournamentElement.getStringFromChild("PLAYERS");

		Module m = Modules.getModuleByName(getModuleName());

		for (String s : playerIDs.split(",")) {
			Player p = CryodexController.getPlayerByID(s);

			if (p != null) {
				IAPlayer xp = (IAPlayer) p.getModuleInfoByModule(m);
				if (xp != null) {
					players.add(xp);
				}
			}
		}

		Element roundElement = tournamentElement.getChild("ROUNDS");

		for (Element e : roundElement.getChildren()) {
			rounds.add(new IARound(e, this));
		}

		name = tournamentElement.getStringFromChild("NAME");

		int counter = 1;
		for (IARound r : rounds) {
			if (r.isSingleElimination()) {
				getTournamentGUI().getRoundTabbedPane()
						.addSingleEliminationTab(r.getMatches().size() * 2,
								r.getPanel());
			} else {
				getTournamentGUI().getRoundTabbedPane().addSwissTab(counter,
						r.getPanel());
				counter++;
			}

		}

		getTournamentGUI().getRankingTable().setPlayers(getAllIAPlayers());
	}

	public IATournament(String name, List<IAPlayer> players,
			InitialSeedingEnum seedingEnum, boolean isSingleElimination) {
		this.name = name;
		this.players = new ArrayList<>(players);
		this.rounds = new ArrayList<>();
		this.seedingEnum = seedingEnum;
		this.startAsSingleElimination = isSingleElimination;

		tournamentGUI = new IATournamentGUI(this);
	}

	public IARound getLatestRound() {
		if (rounds == null || rounds.isEmpty()) {
			return null;
		} else {
			return rounds.get(rounds.size() - 1);
		}
	}

	public int getRoundNumber(IARound round) {
		int count = 0;
		for (IARound r : rounds) {
			count++;
			if (r == round) {
				return count;
			}
		}

		return 0;
	}

	public IARound getRound(int i) {
		if (rounds == null) {
			return null;
		} else {
			return rounds.get(i);
		}
	}

	public IARound getSelectedRound() {
		if (rounds == null) {
			return null;
		} else {
			return getAllRounds().get(
					getTournamentGUI().getRoundTabbedPane().getSelectedIndex());
		}
	}

	public List<IARound> getAllRounds() {
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
		List<IAPlayer> xwPlayers = new ArrayList<>();

		for (Player p : players) {
			IAPlayer xp = new IAPlayer(p);
			xwPlayers.add(xp);
		}

		setIAPlayer(xwPlayers);
	}

	@Override
	public List<Player> getPlayers() {
		List<Player> players = new ArrayList<Player>();

		for (IAPlayer xp : getIAPlayers()) {
			players.add(xp.getPlayer());
		}

		return players;
	}

	public List<IAPlayer> getIAPlayers() {
		return players;
	}

	public void setIAPlayer(List<IAPlayer> players) {
		this.players = players;
	}

	public Set<IAPlayer> getAllIAPlayers() {
		Set<IAPlayer> allPlayers = new TreeSet<IAPlayer>(
				new IAPlayer.RankingComparator(this));

		for (IARound r : getAllRounds()) {
			for (IAMatch m : r.getMatches()) {
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

		for (IAPlayer xp : getAllIAPlayers()) {
			players.add(xp.getPlayer());
		}

		return players;
	}

	@Override
	public IATournamentGUI getTournamentGUI() {
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
			for (IARound r : getAllRounds()) {
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

		if (getLatestRound().isSingleElimination()) {
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
				IARound roundToRemove = rounds.get(index);
				for (IAMatch m : roundToRemove.getMatches()) {
					m.setWinner(null);
					m.setBye(false);
					m.setDraw(false);
					m.setPlayer1(null);
					m.setPlayer2(null);
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

		List<IAMatch> matches;
		if (roundNumber == 1) {

			matches = new ArrayList<IAMatch>();
			List<IAPlayer> tempList = new ArrayList<>();
			tempList.addAll(getIAPlayers());

			List<IAPlayer> firstRoundByePlayers = new ArrayList<>();
			for (IAPlayer p : tempList) {
				if (p.isFirstRoundBye()) {
					firstRoundByePlayers.add(p);
				}
			}
			tempList.removeAll(firstRoundByePlayers);

			if (seedingEnum == InitialSeedingEnum.IN_ORDER) {

				while (tempList.isEmpty() == false) {
					IAPlayer player1 = tempList.get(0);
					IAPlayer player2 = null;
					tempList.remove(0);
					if (tempList.isEmpty() == false) {
						player2 = tempList.get(0);
						tempList.remove(0);
					}

					IAMatch match = new IAMatch(player1, player2);
					matches.add(match);
				}

			} else if (seedingEnum == InitialSeedingEnum.RANDOM) {
				Collections.shuffle(tempList);

				while (tempList.isEmpty() == false) {
					IAPlayer player1 = tempList.get(0);
					IAPlayer player2 = tempList.get(tempList.size() - 1);
					tempList.remove(player1);
					if (player1 == player2) {
						player2 = null;
					} else {
						tempList.remove(player2);
					}

					IAMatch match = new IAMatch(player1, player2);
					matches.add(match);
				}
			} else if (seedingEnum == InitialSeedingEnum.BY_GROUP) {
				Map<String, List<IAPlayer>> playerMap = new HashMap<String, List<IAPlayer>>();

				// Add players to map
				for (IAPlayer p : tempList) {
					List<IAPlayer> playerList = playerMap.get(p.getPlayer()
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
				for (List<IAPlayer> list : playerMap.values()) {
					Collections.shuffle(list);
				}

				// /////////////
				// Add new algorythm here
				// /////////////

				IAPlayer p1 = null;
				IAPlayer p2 = null;
				while (seedValues.isEmpty() == false) {
					int i = 0;
					while (i < seedValues.size()) {
						if (p1 == null) {
							p1 = playerMap.get(seedValues.get(i)).get(0);
						} else {
							p2 = playerMap.get(seedValues.get(i)).get(0);
							matches.add(new IAMatch(p1, p2));
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
					matches.add(new IAMatch(p1, null));
				}
			}

			for (IAPlayer p : firstRoundByePlayers) {
				matches.add(new IAMatch(p, null));
			}

		} else {

			matches = getMatches(getIAPlayers());
		}
		IARound r = new IARound(matches, this, roundNumber);
		rounds.add(r);
		if (roundNumber == 1
				&& startAsSingleElimination
				&& (matches.size() == 1 || matches.size() == 2
						|| matches.size() == 4 || matches.size() == 8
						|| matches.size() == 16 || matches.size() == 32)) {
			r.setSingleElimination(true);
			getTournamentGUI().getRoundTabbedPane().addSingleEliminationTab(
					r.getMatches().size() * 2, r.getPanel());
		} else {
			getTournamentGUI().getRoundTabbedPane().addSwissTab(roundNumber,
					r.getPanel());
		}

		getTournamentGUI().getRankingTable().setPlayers(getAllIAPlayers());
	}

	private List<IAMatch> getMatches(List<IAPlayer> userList) {
		List<IAMatch> matches = new ArrayList<IAMatch>();

		List<IAPlayer> tempList = new ArrayList<IAPlayer>();
		tempList.addAll(userList);
		Collections.sort(tempList, new IAPlayer.PairingComparator(this));

		IAMatch byeMatch = null;
		// Setup the bye match if necessary
		// The player to get the bye is the lowest ranked player who has not had
		// a bye yet or who has the fewest byes
		if (tempList.size() % 2 == 1) {
			IAPlayer byeUser = null;
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
			byeMatch = new IAMatch(byeUser, null);
			tempList.remove(byeUser);
		}

		matches = new IARandomMatchGeneration(this, tempList).generateMatches();

		if (IAMatch.hasDuplicate(matches)) {
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

		List<IAMatch> matches = new ArrayList<>();

		List<IAMatch> matchesCorrected = new ArrayList<IAMatch>();

		if (getLatestRound().isSingleElimination()) {
			List<IAMatch> lastRoundMatches = getLatestRound().getMatches();

			for (int index = 0; index < lastRoundMatches.size(); index = index + 2) {
				IAMatch newMatch = new IAMatch(lastRoundMatches.get(index)
						.getWinner(), lastRoundMatches.get(index + 1)
						.getWinner());
				matches.add(newMatch);
			}

			matchesCorrected = matches;
		} else {
			List<IAPlayer> tempList = new ArrayList<>();
			Collections.sort(tempList, new IAPlayer.PairingComparator(this));
			tempList.addAll(getIAPlayers().subList(0, cutSize));

			while (tempList.isEmpty() == false) {
				IAPlayer player1 = tempList.get(0);
				IAPlayer player2 = tempList.get(tempList.size() - 1);
				tempList.remove(player1);
				if (player1 == player2) {
					player2 = null;
				} else {
					tempList.remove(player2);
				}

				IAMatch match = new IAMatch(player1, player2);
				matches.add(match);
			}

			switch (matches.size()) {
			case 4:
				matchesCorrected.add(matches.get(0));
				matchesCorrected.add(matches.get(3));
				matchesCorrected.add(matches.get(2));
				matchesCorrected.add(matches.get(1));
				break;
			case 8:
				matchesCorrected.add(matches.get(0));
				matchesCorrected.add(matches.get(7));
				matchesCorrected.add(matches.get(4));
				matchesCorrected.add(matches.get(3));
				matchesCorrected.add(matches.get(2));
				matchesCorrected.add(matches.get(5));
				matchesCorrected.add(matches.get(6));
				matchesCorrected.add(matches.get(1));
				break;
			default:
				matchesCorrected = matches;
			}
		}

		IARound r = new IARound(matchesCorrected, this, null);
		r.setSingleElimination(true);
		rounds.add(r);
		getTournamentGUI().getRoundTabbedPane().addSingleEliminationTab(
				cutSize, r.getPanel());

		CryodexController.saveData();
	}

	@Override
	public StringBuilder appendXML(StringBuilder sb) {

		String playerString = "";
		String seperator = "";
		for (IAPlayer p : players) {
			playerString += seperator + p.getPlayer().getSaveId();
			seperator = ",";
		}

		XMLUtils.appendObject(sb, "PLAYERS", playerString);

		XMLUtils.appendList(sb, "ROUNDS", "ROUND", getAllRounds());

		XMLUtils.appendObject(sb, "NAME", name);
		XMLUtils.appendObject(sb, "MODULE", Modules.IA.getName());

		return sb;
	}

	@Override
	public void startTournament() {
		generateRound(1);
	}

	@Override
	public void addPlayer(Player p) {
		IAPlayer xPlayer = new IAPlayer(p);
		getIAPlayers().add(xPlayer);
	}

	@Override
	public void dropPlayer(Player p) {

		IAPlayer xPlayer = null;

		for (IAPlayer xp : getIAPlayers()) {
			if (xp.getPlayer() == p) {
				xPlayer = xp;
				break;
			}
		}

		if (xPlayer != null) {
			getIAPlayers().remove(xPlayer);
		}

		resetRankingTable();
	}

	@Override
	public void resetRankingTable() {
		getTournamentGUI().getRankingTable().setPlayers(getAllIAPlayers());
	}

	@Override
	public Icon getIcon() {
		URL imgURL = IATournament.class.getResource("ia.png");
		if (imgURL == null) {
			System.out.println("fail!!!!!!!!!!");
		}
		ImageIcon icon = new ImageIcon(imgURL);
		return icon;
	}

	@Override
	public String getModuleName() {
		return Modules.IA.getName();
	}
}
