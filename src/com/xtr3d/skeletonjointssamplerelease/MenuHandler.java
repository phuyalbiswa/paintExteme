package com.xtr3d.skeletonjointssamplerelease;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;


public class MenuHandler {
	
	int menuTop;
	int menuLeft;
	int menuRight;
	int menuBottom;
	
	public enum Menus {
		NONE,
		MAIN,
		DOCUMENT,
		OPTIONS,
		COLOR,
		BRUSHES
	};
	
	Menus currentMenu;
	
	private int t;
	
	public MenuHandler()
	{
		
		t=0;
		currentMenu = Menus.MAIN;
		
		menuTop = R.id.top;
		menuLeft = R.id.left;
		menuRight = R.id.right;
		menuBottom = R.id.bottom;
	}
	
	public void processActions(float handLeftX, float handLeftY, float handRightX, float handRightY)
	{
		t++;
		if(t < 20) return;
		// Do actions
		switch(currentMenu)
		{
		case NONE:
			if(handLeftX <= 32 && handLeftY <= 32)
			{
				changeMenu(Menus.MAIN);
				t=0;
			}
			break;
		case MAIN:
			if(handRightY <= 50) //top
			{
				t=0;
				changeMenu(Menus.COLOR);
			}
			else if(handRightX <= 50) //left
			{
				t=0;
				// SAVE
				PaintExtreme.ViewHandler.mCanvasView.saveImage();
				changeMenu(Menus.MAIN);
			}
			else if(handRightX >= 640 - 50)
			{
				t=0;
				changeMenu(Menus.MAIN);
			}
			break;
		case DOCUMENT:
			if(handRightY <= 100) //top
			{
				// TODO Save
				PaintExtreme.ViewHandler.mCanvasView.saveImage();
			}
			else if(handRightX <= 50) //left
			{
				// TODO Delete
			}
			else if(handRightX >= 640 - 50)
			{
				// TODO Open
			}
			else if(handRightY >= 480 - 50)
			{
				// TODO Back
			}
			break;
		case OPTIONS:
			break;
		case COLOR:
			if(handRightY <= 50) //top
			{
				t=0;
				SkeletonDrawer.brushColor = Color.BLUE;
				changeMenu(Menus.MAIN);
			}
			else if(handRightX <= 50) //left
			{
				t=0;
				SkeletonDrawer.brushColor = Color.GREEN;
				changeMenu(Menus.MAIN);
			}
			else if(handRightX >= 640 - 50)
			{
				t=0;
				SkeletonDrawer.brushColor = Color.RED;
				changeMenu(Menus.MAIN);
			}
			else if(handRightY >= 480 - 50)
			{
				t=0;
				changeMenu(Menus.MAIN);
			}
			break;
		case BRUSHES:
			break;
		default:
			break;
		}
	}
	
	public void changeMenu(Menus menu)
	{
		List<Integer> images = getImages(menu);
		PaintExtreme.menuImageTop.setImageResource(images.get(0));
		PaintExtreme.menuImageLeft.setImageResource(images.get(1));
		PaintExtreme.menuImageRight.setImageResource(images.get(2));
		PaintExtreme.menuImageBottom.setImageResource(images.get(3));
		currentMenu = menu;
	}
	
	private List<Integer> getImages(Menus menu)
	{
		List<Integer> images = new ArrayList<Integer>();
		switch(menu)
		{
		case NONE:
			images.add(R.drawable.color);
			images.add(R.drawable.save);
			images.add(R.drawable.back);
			images.add(R.drawable.ic_launcher);
			break;
		case MAIN:
			/*
			images.add(R.drawable.document);
			images.add(R.drawable.color);
			images.add(R.drawable.brushes);
			images.add(R.drawable.menu);
			*/
			images.add(R.drawable.color);
			images.add(R.drawable.save);
			images.add(R.drawable.back);
			images.add(R.drawable.ic_launcher);
			break;
		case DOCUMENT:
			images.add(R.drawable.save);
			images.add(R.drawable.delete);
			images.add(R.drawable.open);
			images.add(R.drawable.open);
			break;
		case OPTIONS:
			images.add(R.drawable.twitter);
			images.add(R.drawable.camera);
			images.add(R.drawable.photo);
			images.add(R.drawable.back);
			break;
		case COLOR:
			images.add(R.drawable.blue_paint);
			images.add(R.drawable.green_paint);
			images.add(R.drawable.red_paint);
			images.add(R.drawable.back);
			break;
		case BRUSHES:
			images.add(R.drawable.brushes);
			images.add(R.drawable.brushes);
			images.add(R.drawable.brushes);
			images.add(R.drawable.brushes);
			break;
		}
		return images;
	}
}
