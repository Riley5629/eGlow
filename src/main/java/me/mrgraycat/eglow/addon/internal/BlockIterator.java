package me.mrgraycat.eglow.addon.internal;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class BlockIterator implements Iterator<Block> {
	private final int maxDistance;
	private static final int gridSize = 16777216;

	private Block[] blockQueue = new Block[3];

	private int currentBlock = 0;
	private int currentDistance = 0;
	private int maxDistanceInt;
	private int secondError;
	private int thirdError;
	private int secondStep;
	private int thirdStep;

	private BlockFace mainFace;
	private BlockFace secondFace;
	private BlockFace thirdFace;

	public BlockIterator(World world, Vector start, Vector direction, int maxDistance) {
		this.maxDistance = maxDistance;
		this.currentDistance = 0;

		double mainDirection = 0.0D;
		double secondDirection = 0.0D;
		double thirdDirection = 0.0D;
		double mainPosition = 0.0D;
		double secondPosition = 0.0D;
		double thirdPosition = 0.0D;

		Block startBlock = world.getBlockAt((int) Math.floor(start.getX()), (int) Math.floor(start.getY()), (int) Math.floor(start.getZ()));

		if (getXLength(direction) > mainDirection) {
			this.mainFace = getXFace(direction);
			mainDirection = getXLength(direction);
			mainPosition = getXPosition(direction, start, startBlock);
			this.secondFace = getYFace(direction);
			secondDirection = getYLength(direction);
			secondPosition = getYPosition(direction, start, startBlock);
			this.thirdFace = getZFace(direction);
			thirdDirection = getZLength(direction);
			thirdPosition = getZPosition(direction, start, startBlock);
		}

		if (getYLength(direction) > mainDirection) {
			this.mainFace = getYFace(direction);
			mainDirection = getYLength(direction);
			mainPosition = getYPosition(direction, start, startBlock);
			this.secondFace = getZFace(direction);
			secondDirection = getZLength(direction);
			secondPosition = getZPosition(direction, start, startBlock);
			this.thirdFace = getXFace(direction);
			thirdDirection = getXLength(direction);
			thirdPosition = getXPosition(direction, start, startBlock);
		}

		if (getZLength(direction) > mainDirection) {
			this.mainFace = getZFace(direction);
			mainDirection = getZLength(direction);
			mainPosition = getZPosition(direction, start, startBlock);
			this.secondFace = getXFace(direction);
			secondDirection = getXLength(direction);
			secondPosition = getXPosition(direction, start, startBlock);
			this.thirdFace = getYFace(direction);
			thirdDirection = getYLength(direction);
			thirdPosition = getYPosition(direction, start, startBlock);
		}

		double d = mainPosition / mainDirection;
		double secondd = secondPosition - secondDirection * d;
		double thirdd = thirdPosition - thirdDirection * d;
		this.secondError = NumberConversions.floor(secondd * 1.6777216E7D);
		this.secondStep = NumberConversions.round(secondDirection / mainDirection * 1.6777216E7D);
		this.thirdError = NumberConversions.floor(thirdd * 1.6777216E7D);
		this.thirdStep = NumberConversions.round(thirdDirection / mainDirection * 1.6777216E7D);
		if (this.secondError + this.secondStep <= 0)
			this.secondError = -this.secondStep + 1;
		if (this.thirdError + this.thirdStep <= 0)
			this.thirdError = -this.thirdStep + 1;
		Block lastBlock = startBlock.getRelative(this.mainFace.getOppositeFace());
		if (this.secondError < 0) {
			this.secondError += gridSize;
			lastBlock = lastBlock.getRelative(this.secondFace.getOppositeFace());
		}
		if (this.thirdError < 0) {
			this.thirdError += gridSize;
			lastBlock = lastBlock.getRelative(this.thirdFace.getOppositeFace());
		}
		this.secondError -= gridSize;
		this.thirdError -= gridSize;
		this.blockQueue[0] = lastBlock;
		this.currentBlock = -1;
		scan();
		boolean startBlockFound = false;
		for (int cnt = this.currentBlock; cnt >= 0; cnt--) {
			if (blockEquals(this.blockQueue[cnt], startBlock)) {
				this.currentBlock = cnt;
				startBlockFound = true;
				break;
			}
		}
		if (!startBlockFound) {
			return;
		}
		this.maxDistanceInt = NumberConversions.round(maxDistance / Math.sqrt(mainDirection * mainDirection + secondDirection * secondDirection + thirdDirection * thirdDirection) / mainDirection);
	}

	private boolean blockEquals(Block a, Block b) {
		return a.getLocation().equals(b.getLocation());
	}

	private BlockFace getXFace(Vector direction) {
		return (direction.getX() > 0.0D) ? BlockFace.EAST : BlockFace.WEST;
	}

	private BlockFace getYFace(Vector direction) {
		return (direction.getY() > 0.0D) ? BlockFace.UP : BlockFace.DOWN;
	}

	private BlockFace getZFace(Vector direction) {
		return (direction.getZ() > 0.0D) ? BlockFace.SOUTH : BlockFace.NORTH;
	}

	private double getXLength(Vector direction) {
		return Math.abs(direction.getX());
	}

	private double getYLength(Vector direction) {
		return Math.abs(direction.getY());
	}

	private double getZLength(Vector direction) {
		return Math.abs(direction.getZ());
	}

	private double getPosition(double direction, double position, int blockPosition) {
		return (direction > 0.0D) ? (position - blockPosition) : ((blockPosition + 1) - position);
	}

	private double getXPosition(Vector direction, Vector position, Block block) {
		return getPosition(direction.getX(), position.getX(), block.getX());
	}

	private double getYPosition(Vector direction, Vector position, Block block) {
		return getPosition(direction.getY(), position.getY(), block.getY());
	}

	private double getZPosition(Vector direction, Vector position, Block block) {
		return getPosition(direction.getZ(), position.getZ(), block.getZ());
	}

	@Override
	public boolean hasNext() {
		scan();
		return (this.currentBlock != -1);
	}

	@Override
	public Block next() throws NoSuchElementException {
		scan();
		if (this.currentBlock <= -1)
			throw new NoSuchElementException();
		return this.blockQueue[this.currentBlock--];
	}

	private void scan() {
		if (this.currentBlock >= 0)
			return;

		if (this.maxDistance != 0 && this.currentDistance > this.maxDistanceInt)
			return;

		this.currentDistance++;
		this.secondError += this.secondStep;
		this.thirdError += this.thirdStep;
		if (this.secondError > 0 && this.thirdError > 0) {
			this.blockQueue[2] = this.blockQueue[0].getRelative(this.mainFace);
			if (this.secondStep * this.thirdError < this.thirdStep * this.secondError) {
				this.blockQueue[1] = this.blockQueue[2].getRelative(this.secondFace);
				this.blockQueue[0] = this.blockQueue[1].getRelative(this.thirdFace);
			} else {
				this.blockQueue[1] = this.blockQueue[2].getRelative(this.thirdFace);
				this.blockQueue[0] = this.blockQueue[1].getRelative(this.secondFace);
			}
			this.thirdError -= gridSize;
			this.secondError -= gridSize;
			this.currentBlock = 2;
			return;
		}
		if (this.secondError > 0) {
			this.blockQueue[1] = this.blockQueue[0].getRelative(this.mainFace);
			this.blockQueue[0] = this.blockQueue[1].getRelative(this.secondFace);
			this.secondError -= gridSize;
			this.currentBlock = 1;
			return;
		}
		if (this.thirdError > 0) {
			this.blockQueue[1] = this.blockQueue[0].getRelative(this.mainFace);
			this.blockQueue[0] = this.blockQueue[1].getRelative(this.thirdFace);
			this.thirdError -= gridSize;
			this.currentBlock = 1;
			return;
		}
		this.blockQueue[0] = this.blockQueue[0].getRelative(this.mainFace);
		this.currentBlock = 0;
	}
}