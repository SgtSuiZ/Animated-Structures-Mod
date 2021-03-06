package com.awesomeholden.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.glu.GLU;

import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;

import com.awesomeholden.Main;
import com.awesomeholden.Tileentities.TileentityAnimatedClient;
import com.awesomeholden.Tileentities.TileentityAnimatedServer;
import com.awesomeholden.Tileentities.TileentityAnimationEditorClient;
import com.awesomeholden.Tileentities.TileentityAnimationEditorServer;
import com.awesomeholden.packets.RemoveEditorClient;
import com.awesomeholden.packets.SendTileentityAnimatedTextureUpdate;
import com.awesomeholden.packets.SetCoordsOnClient;
import com.awesomeholden.packets.UpdateControllerClientTextures;
import com.awesomeholden.proxies.ClientProxy;
import com.awesomeholden.proxies.ServerProxy;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;

public class AnimationControllerServer { //Pointers to objects inside of ServerProxy.AnimationControllers
		
	public int TileentitiesToBeAdded = 4;
	
	public int dimension = 0;
		
	public List<Integer> frameIntervals = new ArrayList<Integer>();
	
	public int[] coords;
	public int tick = 0;
	public List<TileentityAnimatedServer> theControlled = new ArrayList<TileentityAnimatedServer>();
	
	public List<HashMap<Integer,List<Integer>>> framesInfo = new ArrayList<HashMap<Integer,List<Integer>>>();
	
	public AnimationControllerServer(){
		coords = new int[]{0,0,0,0,0,0};
		
		frameIntervals.add(10);
		framesInfo.add(new HashMap<Integer,List<Integer>>());
	}
	
	public int frameIndex = 0;
		
	public void onUpdate(){ //added to ServerLoop
				
		if(tick == frameIntervals.get(frameIndex)){
			
			if(TileentitiesToBeAdded>0){
				TileentitiesToBeAdded--;
								
				WorldServer world = MinecraftServer.getServer().worldServers[dimension];
				for(int ph=0;ph<world.loadedTileEntityList.size();ph++){
					TileentityAnimatedServer c;
					if(world.loadedTileEntityList.get(ph) instanceof TileentityAnimatedServer){
						c = (TileentityAnimatedServer) world.loadedTileEntityList.get(ph);
						if(coords[0]<=c.xCoord && coords[1]<=c.yCoord && coords[2]<=c.zCoord && coords[3]>=c.xCoord && coords[4]>=c.yCoord && coords[5]>=c.zCoord)
							ServerProxy.addTileentityToController(c, this);
					}else{
						continue;
					}
				}
				
				List<Integer> ls = new ArrayList<Integer>();
				List<TileentityAnimatedServer> ls2 = new ArrayList<TileentityAnimatedServer>();
				for(int i=0;i<theControlled.size();i++){
					int id = ServerProxy.getTileentityAnimatedId(theControlled.get(i));
					
					if(ls.indexOf(id)<0){
						ls.add(id);
						ls2.add(theControlled.get(i));
					}
				}
				theControlled = ls2;
			}
																					
			//System.out.println("CONTROLLERSERVER COORDS: "+Arrays.toString(coords)+" THECONTROLLED SIZE: "+theControlled.size());
			/*HashMap<Integer,List<Integer>> stuff = new HashMap<Integer,List<Integer>>();
			
			for(int i=0;i<theControlled.size();i++){
				TileentityAnimatedServer c = theControlled.get(i);
				
				try{
					int tex = c.frames.get(frameIndex);
				
				List<Integer> ls = stuff.get(tex);
				
				if(ls == null){
					ls = new ArrayList<Integer>();
					stuff.put(tex, ls);
				}
				
				if(frameIndex == 0){
					if(tex != c.frames.get(c.frames.size()-1))
						ls.add(i);
				}else{
					if(tex != c.frames.get(frameIndex-1))
						ls.add(i);
				}
				}catch(IndexOutOfBoundsException e){
					c.frames.clear();
					for(int i2=0;i2<frameIntervals.size();i2++)
						c.frames.add(0);
				}
			}*/
			
			if(coords != null){
				int i = frameIndex+1;
				if(i==frameIntervals.size())
					i = 0;
				
				//System.out.println("SERVER SIZE: "+ServerProxy.AnimationControllers.size());
				
				Main.network.sendToAllAround(new UpdateControllerClientTextures(coords,framesInfo.get(i)),genTargetPoint());
			}
				
			/*theControlled.clear();
			for(int i=0;i<MinecraftServer.getServer().worldServers[dimension].loadedTileEntityList.size();i++){
				if(!(MinecraftServer.getServer().worldServers[dimension].loadedTileEntityList.get(i) instanceof TileentityAnimatedServer))
					continue;
				
				TileentityAnimatedServer c = (TileentityAnimatedServer) MinecraftServer.getServer().worldServers[dimension].loadedTileEntityList.get(i);
				
				if(coords[0]<=c.xCoord && coords[3]>=c.xCoord && coords[1]<=c.yCoord && coords[4]>=c.yCoord && coords[2]<=c.zCoord && coords[5]>=c.zCoord)
					theControlled.add(c);
			}*/
			
		
			if(frameIndex+1 == frameIntervals.size()){
				frameIndex = 0;
			}else{
				frameIndex++;
			}
			
			tick = -1;
			
		}
		
		tick++;
	}
	
	public TargetPoint genTargetPoint(){
		return new TargetPoint(dimension,(coords[0]+coords[3])/2,(coords[1]+coords[4])/2,(coords[2]+coords[5])/2,80);
	}
	
	public void onCoordsSet(){
		
		/*if(coords != null)
			Main.network.sendToAll(new SetCoordsOnClient());*/
		
		int i2 = ServerProxy.AnimationControllers.indexOf(this);
		for(int i=0;i<ServerProxy.AnimationControllers.size();i++){
			AnimationControllerServer c = ServerProxy.AnimationControllers.get(i);
			if(i != i2 && Main.compareArrays(coords, c.coords))
				ServerProxy.AnimationControllers.remove(i);
		}
		
		boolean doo = true;
		for(int ph=0;ph<ServerProxy.controllerAssignmentCache.size();ph++){
			TileentityAnimatedServer c = ServerProxy.controllerAssignmentCache.get(ph);
			int id = ServerProxy.getTileentityAnimatedId(c);
			if(coords[0]<=c.xCoord && coords[1]<=c.yCoord && coords[2]<=c.zCoord && coords[3]>=c.xCoord && coords[4]>=c.yCoord && coords[5]>=c.zCoord){
				for(int ph2=0;ph2<theControlled.size();ph2++){
					if(id<ServerProxy.getTileentityAnimatedId(theControlled.get(ph2))){
						theControlled.add(ph2,c);
						doo = false;
						break;
					}
				}
				if(doo){
					theControlled.add(c);
				}
				doo = true;
			}
		}
	}
	
	public void removeTileentity(int x,int y,int z){
		int index = -1;
		List<TileentityAnimatedServer> n = new ArrayList<TileentityAnimatedServer>();
		for(int i=0;i<theControlled.size();i++){
			TileentityAnimatedServer c = theControlled.get(i);
			
			if(c.xCoord == x && c.yCoord == y && c.zCoord == z){
				index = i;
			}else{
				n.add(c);
			}
		}
		
		if(index == -1)
			return;
		
		theControlled = n;
		
		for(int i=0;i<framesInfo.size();i++){
			for(Entry<Integer, List<Integer>> e : framesInfo.get(i).entrySet()){
				for(int i2=0;i2<e.getValue().size();i2++){
					if(e.getValue().get(i2) == index){
						e.getValue().remove(i2);
						i2--;
					}else if(e.getValue().get(i2) > index){
						e.getValue().set(i2, e.getValue().get(i2)-1);
					}
				}
			}
		}
		
	}

}
