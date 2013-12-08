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
	
	public MenuHandler()
	{
		currentMenu = Menus.NONE;
		
		menuTop = R.id.top;
		menuLeft = R.id.left;
		menuRight = R.id.right;
		menuBottom = R.id.bottom;
	}
	
	public void processActions(float handLeftX, float handLeftY, float handRightX, float handRightY)
	{
		// Do actions
		switch(currentMenu)
		{
		case NONE:
			if(handLeftX <= 32 && handLeftY <= 32)
			{
				changeMenu(Menus.MAIN);
			}
			break;
		case MAIN:
			if(handRightY <= 50) //top
			{
				changeMenu(Menus.DOCUMENT);
			}
			else if(handRightX <= 50) //left
			{
				changeMenu(Menus.COLOR);
			}
			else if(handRightX >= 640 - 50)
			{
				changeMenu(Menus.BRUSHES);
			}
			else if(handRightY >= 480 - 50)
			{
				changeMenu(Menus.OPTIONS);
			}
			break;
		case DOCUMENT:
			if(handRightY <= 50) //top
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
				SkeletonDrawer.brushColor = Color.RED;
			}
			else if(handRightX <= 50) //left
			{
				SkeletonDrawer.brushColor = Color.GREEN;
			}
			else if(handRightX >= 640 - 50)
			{
				SkeletonDrawer.brushColor = Color.BLUE;
			}
			else if(handRightY >= 480 - 50)
			{
				SkeletonDrawer.brushColor = Color.YELLOW;
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
			images.add(R.drawable.document);
			images.add(R.drawable.color);
			images.add(R.drawable.brushes);
			images.add(R.drawable.menu);
			break;
		case MAIN:
			images.add(R.drawable.document);
			images.add(R.drawable.color);
			images.add(R.drawable.brushes);
			images.add(R.drawable.menu);
			break;
		case DOCUMENT:
			images.add(R.drawable.save);
			images.add(R.drawable.delete);
			images.add(R.drawable.open);
			images.add(R.drawable.open);
			break;
		case OPTIONS:
			images.add(R.drawable.delete);
			images.add(R.drawable.delete);
			images.add(R.drawable.delete);
			images.add(R.drawable.delete);
			break;
		case COLOR:
			images.add(R.drawable.blue_paint);
			images.add(R.drawable.green_paint);
			images.add(R.drawable.red_paint);
			images.add(R.drawable.back);
			break;
		case BRUSHES:
			images.add(R.drawable.open);
			images.add(R.drawable.open);
			images.add(R.drawable.open);
			images.add(R.drawable.open);
			break;
		}
		return images;
	}
}
