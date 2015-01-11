package trial;

import battlecode.common.*;
import java.util.*;
import java.math.*;

public class RobotPlayer {
	static RobotController me;
	static Team goodGuys;
	static Team badGuys;
	static int atkRange;
	static int sightRange;
	static Random rand;
	static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	
	public static void run(RobotController owl) {
		me = owl;
		rand = new Random(me.getID());
		MapLocation badHQ = me.senseEnemyHQLocation();
		MapLocation[] badTowers = me.senseEnemyTowerLocations();
		atkRange = me.getType().attackRadiusSquared;
		sightRange = me.getType().sensorRadiusSquared;
		goodGuys = me.getTeam();
		badGuys = goodGuys.opponent();
		
		while (true) {
			
			try {
				me.setIndicatorString(0, me.getType().toString());
			} catch (Exception e) {
				System.out.println("Unexpected exception");
				e.printStackTrace();
			}
			
			if (me.getType() == RobotType.HQ) {
				try {
					System.out.println(me.senseRobotAtLocation(HQTransfer()));
					nearATK();
					trySpawn(directions[rand.nextInt(8)], RobotType.BEAVER);
				} catch (Exception e) {
					System.out.println("HQ Exception");
					System.out.println(me.getType() + " " + Clock.getBytecodesLeft());
                    e.printStackTrace();
				}
			} else {
				try {
					System.out.println(me.senseRobotAtLocation(unitTransfer()));
				} catch (Exception e) {
					System.out.println("Non-HQ Transfer Exception");
					System.out.println(me.getType() + " " + Clock.getBytecodesLeft());
					e.printStackTrace();
				}
			}
			
			if (me.getType() == RobotType.TOWER) {
				try {
					nearATK();
				} catch (Exception e) {
					System.out.println("TOWER Exception");
					e.printStackTrace();
				}
			}
			
			if (me.getType() == RobotType.BEAVER) {
				try {
//					randMove(directions[rand.nextInt(8)]);
					nearATK();
					tryMine();
					HQMove(badHQ);
				} catch (Exception e) {
					System.out.println("BEAVER Exception");
					e.printStackTrace();
				}
			}
		}
	}
	
	static MapLocation HQTransfer() throws GameActionException {
		RobotInfo[] nearGoods = me.senseNearbyRobots(GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, goodGuys);
		double sup = me.getSupplyLevel();
		if (nearGoods.length > 1 && sup >= 1) {
			MapLocation home = me.senseHQLocation();
			for (int i = nearGoods.length - 1; i > 0; i--) {
				if (nearGoods[i].supplyLevel < nearGoods[i-1].supplyLevel || (int) nearGoods[i].supplyLevel == (int) nearGoods[i-1].supplyLevel && nearGoods[i].location.distanceSquaredTo(home) > nearGoods[i-1].location.distanceSquaredTo(home)) {
					RobotInfo tmp = nearGoods[i];
					nearGoods[i] = nearGoods[i-1];
					nearGoods[i-1] = tmp;
				}
			}
			
			if (nearGoods.length > 2) {
				for (int i = nearGoods.length - 1; i > 1; i--) {
					if (nearGoods[i].supplyLevel < nearGoods[i-1].supplyLevel || (int) nearGoods[i].supplyLevel == (int) nearGoods[i-1].supplyLevel && nearGoods[i].location.distanceSquaredTo(home) > nearGoods[i-1].location.distanceSquaredTo(home)) {
						RobotInfo tmp = nearGoods[i];
						nearGoods[i] = nearGoods[i-1];
						nearGoods[i-1] = tmp;
					}
				}
			}
			
			for (int i = 0; i < 2; i++) {
				me.transferSupplies((int) sup/2, nearGoods[i].location);
			}
		} else if (nearGoods.length > 0 && sup >= 1) {
			me.transferSupplies((int) sup, nearGoods[0].location);
		}
		
		if (nearGoods.length > 0) {
			return nearGoods[0].location;
		} else {
			return null;
		}
	}
	
	static MapLocation unitTransfer() throws GameActionException {
		RobotInfo[] nearGoods = me.senseNearbyRobots(GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, goodGuys);
		double sup = me.getSupplyLevel();
		if (nearGoods.length > 1 && sup >= 1) {
			MapLocation home = me.senseHQLocation();
			for (int i = nearGoods.length - 1; i > 0; i--) {
				if (nearGoods[i].supplyLevel < nearGoods[i-1].supplyLevel || (int) nearGoods[i].supplyLevel == (int) nearGoods[i-1].supplyLevel && nearGoods[i].location.distanceSquaredTo(home) > nearGoods[i-1].location.distanceSquaredTo(home)) {
					RobotInfo tmp = nearGoods[i];
					nearGoods[i] = nearGoods[i-1];
					nearGoods[i-1] = tmp;
				}
			}
		}
		
		if (nearGoods.length > 0 && sup >= 1) {
			if (me.getType() == RobotType.TOWER) {
				me.transferSupplies((int) sup, nearGoods[0].location);
			} else if (sup > nearGoods[0].supplyLevel) {
				me.transferSupplies((int) Math.floor((sup - nearGoods[0].supplyLevel) / 1.5), nearGoods[0].location);
			}
		}
		
		if (nearGoods.length > 0) {
			return nearGoods[0].location;
		} else {
			return null;
		}
	}
	
	static void nearATK() throws GameActionException {
		RobotInfo[] nearBads = me.senseNearbyRobots(atkRange, badGuys);
		
		if (nearBads.length > 0) {
			MapLocation target = nearBads[0].location;
			
			if (nearBads.length > 1) {
				for (int i = 1; i < nearBads.length; i++) {
					if (nearBads[i].location.distanceSquaredTo(me.getLocation()) < target.distanceSquaredTo(me.getLocation())) {
						target = nearBads[i].location;
					}
				}
			}
			
			if (me.isWeaponReady()) {
				me.attackLocation(target);
			}
		}
	}
	
	static void mineMove() throws GameActionException{
		
	}
	
	static void HQMove(MapLocation bad) throws GameActionException {
		Direction toHQ = me.getLocation().directionTo(bad);
		
		if (me.canMove(toHQ) && me.isCoreReady()) {
			me.move(toHQ);
		}
	}
	
	static void randMove(Direction d) throws GameActionException {
		int dIndex = 0;
		while (dIndex < 7 && !me.canMove(directions[dIndex])) {
			dIndex+=2;
		}
		if (dIndex < 7) {
			me.move(directions[dIndex]);
		}
	}
	
	static void trySpawn(Direction d, RobotType type) throws GameActionException {
		int dIndex = 0;
		while (dIndex < 7 && !me.canSpawn(directions[dIndex], type)) {
			dIndex++;
		}
		if (dIndex < 7 && me.isCoreReady()) {
			me.spawn(directions[dIndex], type);
		}
	}
	
	static void tryMine() throws GameActionException {
		if (me.senseOre(me.getLocation()) > 10 && me.canMine() && me.isCoreReady()) {
			me.mine();
		}
	}
}
