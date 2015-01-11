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
					HQTransfer();
					nearATK();
					trySpawn(directions[rand.nextInt(8)], RobotType.BEAVER);
				} catch (Exception e) {
					System.out.println("HQ Exception");
                    e.printStackTrace();
				}
			} else {
				try {
					unitTransfer();
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
			
			me.yield();
		}
	}
	
	static void HQTransfer() throws GameActionException {
//		System.out.println(Clock.getBytecodesLeft() + " Transfer start");
		RobotInfo[] nearGoods = me.senseNearbyRobots(GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, goodGuys);
//		System.out.println(Clock.getBytecodesLeft() + " Sense robots");
		double sup = me.getSupplyLevel();
//		System.out.println(Clock.getBytecodesLeft() + " Check supply");
		if (nearGoods.length > 1 && sup >= 1) {
			MapLocation home = me.senseHQLocation();
	//		System.out.println(Clock.getBytecodesLeft() + " Sense HQ");
			for (int i = nearGoods.length - 1; i > 0; i--) {
				if (nearGoods[i].supplyLevel < nearGoods[i-1].supplyLevel || (int) nearGoods[i].supplyLevel == (int) nearGoods[i-1].supplyLevel && nearGoods[i].location.distanceSquaredTo(home) > nearGoods[i-1].location.distanceSquaredTo(home)) {
					RobotInfo tmp = nearGoods[i];
					nearGoods[i] = nearGoods[i-1];
					nearGoods[i-1] = tmp;
				}
		//		System.out.println(Clock.getBytecodesLeft() + " Sort robots A " + i);
			}
			
			if (nearGoods.length > 2) {
				for (int i = nearGoods.length - 1; i > 1; i--) {
					if (nearGoods[i].supplyLevel < nearGoods[i-1].supplyLevel || (int) nearGoods[i].supplyLevel == (int) nearGoods[i-1].supplyLevel && nearGoods[i].location.distanceSquaredTo(home) > nearGoods[i-1].location.distanceSquaredTo(home)) {
						RobotInfo tmp = nearGoods[i];
						nearGoods[i] = nearGoods[i-1];
						nearGoods[i-1] = tmp;
					}
			//		System.out.println(Clock.getBytecodesLeft() + " Sort robots B " + i);
				}
			}
			
			for (int i = 0; i < 2; i++) {
				if (Clock.getBytecodesLeft() > 525) {
					me.transferSupplies((int) sup/2, nearGoods[i].location);
			//		System.out.println(Clock.getBytecodesLeft() + " Transfer complete " + i);
				}
			}
		} else if (nearGoods.length > 0 && sup >= 1 && Clock.getBytecodesLeft() > 525) {
	//		System.out.println(Clock.getBytecodesLeft() + " Check bytecodes left");
			me.transferSupplies((int) sup, nearGoods[0].location);
	//		System.out.println(Clock.getBytecodesLeft() + "Transfer complete");
		}
	}
	
	static void unitTransfer() throws GameActionException {
//		System.out.println(Clock.getBytecodesLeft() + " Transfer start");
		RobotInfo[] nearGoods = me.senseNearbyRobots(GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, goodGuys);
//		System.out.println(Clock.getBytecodesLeft() + " Sense robots");
		double sup = me.getSupplyLevel();
//		System.out.println(Clock.getBytecodesLeft() + " Check supply");
		if (nearGoods.length > 1 && sup >= 1) {
			MapLocation home = me.senseHQLocation();
	//		System.out.println(Clock.getBytecodesLeft() + " Sense HQ");
			for (int i = nearGoods.length - 1; i > 0; i--) {
				if (nearGoods[i].supplyLevel < nearGoods[i-1].supplyLevel || (int) nearGoods[i].supplyLevel == (int) nearGoods[i-1].supplyLevel && nearGoods[i].location.distanceSquaredTo(home) > nearGoods[i-1].location.distanceSquaredTo(home)) {
					RobotInfo tmp = nearGoods[i];
					nearGoods[i] = nearGoods[i-1];
					nearGoods[i-1] = tmp;
				}
		//		System.out.println(Clock.getBytecodesLeft() + " Sort robots " + i);
			}
		}
		
		if (nearGoods.length > 0 && sup >= 1 && Clock.getBytecodesLeft() > 525) {
	//		System.out.println(Clock.getBytecodesLeft() + " Check bytecodes left");
			if (me.getType() == RobotType.TOWER) {
		//		System.out.println(Clock.getBytecodesLeft() + " Check if Tower type");
				me.transferSupplies((int) sup, nearGoods[0].location);
		//		System.out.println(Clock.getBytecodesLeft() + " Transfer complete");
			} else if (sup > nearGoods[0].supplyLevel) {
		//		System.out.println(Clock.getBytecodesLeft() + " Check if Tower type");
				me.transferSupplies((int) Math.floor((sup - nearGoods[0].supplyLevel) / 1.5), nearGoods[0].location);
		//		System.out.println(Clock.getBytecodesLeft() + "Transfer complete");
			}
		}
	}
	
	static void nearATK() throws GameActionException {
//		System.out.println(Clock.getBytecodesLeft() + " atk start");
		RobotInfo[] nearBads = me.senseNearbyRobots(atkRange, badGuys);
//		System.out.println(Clock.getBytecodesLeft() + " Sense robots");
		
		if (nearBads.length > 0) {
			MapLocation target = nearBads[0].location;
			
			if (nearBads.length > 1) {
				for (int i = 1; i < nearBads.length; i++) {
					if (nearBads[i].location.distanceSquaredTo(me.getLocation()) < target.distanceSquaredTo(me.getLocation())) {
						target = nearBads[i].location;
					}
			//		System.out.println(Clock.getBytecodesLeft() + " Sort robots");
				}
			}
			
			if (me.isWeaponReady()) {
		//		System.out.println(Clock.getBytecodesLeft() + " Check weapon");
				me.attackLocation(target);
		//		System.out.println(Clock.getBytecodesLeft() + " atk complete");
			}
		}
	}
	
	static void mineMove() throws GameActionException{
		
	}
	
	static void HQMove(MapLocation bad) throws GameActionException {
//		System.out.println(Clock.getBytecodesLeft() + " Move start");
		Direction toHQ = me.getLocation().directionTo(bad);
//		System.out.println(Clock.getBytecodesLeft() + " get HQ direction");
		
		if (me.canMove(toHQ) && me.isCoreReady()) {
	//		System.out.println(Clock.getBytecodesLeft() + " Check Move and ready");
			me.move(toHQ);
	//		System.out.println(Clock.getBytecodesLeft() + " Move complete");
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
//		System.out.println(Clock.getBytecodesLeft() + " Spawn start");
		int dIndex = 0;
		while (dIndex < 7 && !me.canSpawn(directions[dIndex], type)) {
	//		System.out.println(Clock.getBytecodesLeft() + " Check Spawn");
			dIndex++;
		}
		if (dIndex < 7 && me.isCoreReady()) {
	//		System.out.println(Clock.getBytecodesLeft() + " Check ready");
			me.spawn(directions[dIndex], type);
	//		System.out.println(Clock.getBytecodesLeft() + " Spawn complete");
		}
	}
	
	static void tryMine() throws GameActionException {
//		System.out.println(Clock.getBytecodesLeft() + " Mine start");
		if (me.senseOre(me.getLocation()) > 10 && me.canMine() && me.isCoreReady()) {
	//		System.out.println(Clock.getBytecodesLeft() + " Sense ore + Check location + Check mine + Check ready");
			me.mine();
	//		System.out.println(Clock.getBytecodesLeft() + " Mine complete");
		}
	}
}
