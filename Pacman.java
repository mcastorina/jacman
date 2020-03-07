import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.sound.sampled.*;
import javax.imageio.*;

public class Pacman extends JPanel implements KeyListener , MouseListener
{
	private static JFrame frame;
	
	//grid
	public static int x,y,steps,direction;
	private static int eaten;
	private static long timeEaten;
	private static Color rand;
	private static Location pacSpawn,deadPos;
	
	public static int score,lives,level,pellets;
	public static boolean laser,gameOver;
	public static Location maxReach;
	
	private static final int SPECIAL_DELAY = 5000;
	private static final int SCORE_INCREMENT = 10;
	private static final int SPECIAL_INCREMENT = 50;
	private static int MOVE_DELAY;
	
	//movement
	private static long startTime,lastMoved,lastBlinked,deadTime;
	
	private static boolean goNorth,goSouth,goWest,goEast;
	private static boolean tryNorth,trySouth,tryWest,tryEast;
	private static boolean valid,isCherry;
	
	private char north,south,east,west,curPos;
	
	private String tele1,tele2;
	public static char [][] grid;

	private static int page;
	
	//start page
	private Color startColor;
	private boolean dull;
	private static Point frameLoc;
	private static Image pac,cherry;
	
	//credits
	private static Image credits;
	private static int finalTime;

	//music
	private static File startFile,gameFile,creditFile,pacDies;
	private static AudioInputStream stream;
	private static Clip music,musicFX;

	//ghost	
	private static Ghost blinky,pinky,inky,clyde;
	private static ArrayList<Ghost> ghosts;
	public static int numEaten;
	public static Location blinkySpawn,pinkySpawn,inkySpawn,clydeSpawn;
	
	//next level
	private static boolean nextLevel;

	//credits

	public Pacman()
	{
		init();
	}
	public void init()
	{
		pellets = 0;
		grid = new char [28][19];
		try
		{
			Scanner in = new Scanner(new File("Grid.dat"));
			for (int k = 0; k < 28; k++)
			{
				String s = in.nextLine();
				for (int i = 0; i < s.length(); i++)
				{
					grid[k][i] = s.charAt(i);
					switch (s.charAt(i))
					{
						case '1' : tele1 = k + " " + i; break;
						case '2' : tele2 = k + " " + i; break;
						case 'd' : clydeSpawn = new Location(i,k); break;
						case 'b' : blinkySpawn = new Location(i,k); break;
						case 'p' : pinkySpawn = new Location(i,k); break;
						case 'i' : inkySpawn = new Location(i,k); break;
						case 'P' : pacSpawn = new Location(i,k); break;
						case '.' : pellets++; break;
						case 's' : pellets++; break;
					}	
				}
			}
		}catch(Exception e){}
		
		x = pacSpawn.getX();
		y = pacSpawn.getY();

		steps = 0;
		eaten = 0;

		tryNorth = trySouth = tryEast = tryWest = false;
		goNorth = goSouth = goEast = goWest = false;

		blinky = new Ghost(Ghost.BLINKY);
		pinky = new Ghost(Ghost.PINKY);
		inky = new Ghost(Ghost.INKY);
		clyde = new Ghost(Ghost.CLYDE);

		ghosts = new ArrayList<Ghost>();
		ghosts.add(blinky);
		ghosts.add(pinky);
		ghosts.add(inky);
		ghosts.add(clyde);
		
		valid = false;
		MOVE_DELAY = 175;
		
		laser = isCherry = gameOver = false;
		maxReach = new Location(0,0);
		
		rand = new Color((int)(Math.random()*256),(int)(Math.random()*256),(int)(Math.random()*256));
		
		if (!nextLevel)
		{
			score = 0;
			lives = 3;
			level = 1;

			page = 1;
			dull = true;
			startColor = Color.GREEN;

			startFile = new File("01 Anger Of The Earth.wav");
			gameFile = new File("PacFever.wav");
			creditFile = new File("WeirdAl Pac.wav");
			pacDies = new File("PacManDies.wav");
		}
		else
		{
			score+=500;
			level++;
			
			if (lives < 3)
				lives++;
		}
		update();

		nextLevel = false;
	}
	public static void dead()
	{
		if(lives > 0)
		{
			lives--;
			x = pacSpawn.getX();
			y = pacSpawn.getY();
			
			blinky = new Ghost(Ghost.BLINKY);
			pinky = new Ghost(Ghost.PINKY);
			inky = new Ghost(Ghost.INKY);
			clyde = new Ghost(Ghost.CLYDE);
			
			ghosts = new ArrayList<Ghost>();
			ghosts.add(blinky);
			ghosts.add(pinky);
			ghosts.add(inky);
			ghosts.add(clyde);
		}
		else
		{
			gameOver = true;
			finalTime = (int)((System.currentTimeMillis()-startTime)/1000);
		}
	}
	public static void deadIncrement(int numeaten)
	{
		score+=(numeaten*100);
	}

	public void paintComponent(Graphics g)
	{
		switch(page)
		{
			case 1 : drawStartScreen(g); break;
			case 2 : drawGameScreen(g); break;
			case 3 : drawCredits(g); break;
		}
	}
	private void drawPac(Graphics g)
	{
		g.setColor(Color.YELLOW);

		if (steps % 2 == 0)
			g.fillOval(x*25+2,y*25+2,22,22);
		else
		{
			if (goEast)
				g.fillArc(x*25+2,y*25+2,22,22,45,270);
			if (goWest)
				g.fillArc(x*25+2,y*25+2,22,22,225,270);
			if (goNorth)
				g.fillArc(x*25+2,y*25+2,22,22,125,270);
			if (goSouth)
				g.fillArc(x*25+2,y*25+2,22,22,315,270);
		}
	}
	private void drawDeadPac(Graphics g)
	{
		int a = deadPos.getX()*25+2;
		int b = deadPos.getY()*25+2;
		g.setColor(Color.YELLOW);

		long time = System.currentTimeMillis()-deadTime;
		int increment = 0;
		
		switch (direction)
		{
			case 0 : increment = 90; break;
			case 1 : increment = 0; break;
			case 2 : increment = -90; break;
			case 3 : increment = 180; break;
		}
		
		if (time < 100)
			g.fillArc(a,b,22,22,60+increment,240);
		else if (time < 200)
			g.fillArc(a,b,22,22,75+increment,210);
		else if (time < 300)
			g.fillArc(a,b,22,22,90+increment,180);
		else if (time < 400)
			g.fillArc(a,b,22,22,105+increment,150);
		else if (time < 500)
			g.fillArc(a,b,22,22,120+increment,120);
		else if (time < 600)
			g.fillArc(a,b,22,22,135+increment,90);
		else if (time < 700)
			g.fillArc(a,b,22,22,150+increment,60);
		else if (time < 800)
			g.fillArc(a,b,22,22,175+increment,30);
		else if (time < 900)
			g.fillArc(a,b,22,22,180+increment,1);
		else if (time >= 900 && gameOver)
		{
			page = 3;
			stop();
			try{play(3);}catch(Exception e){}
		}
	}
	private void fill(Graphics g)
	{
		for (int k = 0; k < grid.length; k++)
			for (int i = 0; i < grid[k].length; i++)
			{
				g.setColor(Color.BLACK);
				g.fillRect(i*25,k*25,25,25);

				g.setColor(color(grid[k][i]));
				if (grid[k][i] == '.' || grid[k][i] == 's')
				{
					if (grid[k][i] == '.')
						g.fillOval(i*25+8,k*25+8,10,10);
					else
					{
						if (System.currentTimeMillis() >= lastBlinked + 600)
							lastBlinked = System.currentTimeMillis();
						else if (System.currentTimeMillis() >= lastBlinked + 300)
							g.setColor(Color.black);
						g.fillOval(i*25+4,k*25+4,18,18);
					}
				}
				else if (grid[k][i] == 'd')
					g.fillRect(i*25,k*25,25,5);
				else if (grid[k][i] == 'w')
					g.fillRect(i*25,k*25,25,25);
				else if (grid[k][i] == 'C')
					g.drawImage(cherry,i*25,k*25,null);
				else
					g.fillRect(i*25,k*25,25,25);
			}
	}
	private void drawStartScreen(Graphics g)
	{
		//pointer on screen
		PointerInfo loc = MouseInfo.getPointerInfo();
		int a = (int)(loc.getLocation().getX() - frameLoc.getX());
		int b = (int)(loc.getLocation().getY() - frameLoc.getY());

		if (a >= 140 && a <= 340 && b >= 570 && b <= 670)
		{
			frame.setCursor(new Cursor(Cursor.HAND_CURSOR));
			startColor = Color.CYAN;
		}
		else
		{
			frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			startColor = Color.GREEN;
		}

		//image background
		g.drawImage(pac,0,0,null);
		//start button
		g.setColor(startColor);
		g.fillRect(0,595,500,10);
		g.fillRect(135,545,200,100);
		//middle of start button
		g.setColor(Color.BLACK);
		g.fillRect(145,555,180,80);
		
		//text
		g.setFont(new Font("OCR A Extended",Font.PLAIN,50));
		g.setColor(startColor);
		g.drawString("START",160,610);

		g.setFont(new Font("OCR A Extended",Font.PLAIN,80));
		g.setColor(Color.GREEN);
		g.drawString("Jacman",90,110);

		//shade
		if (dull)
		{
			g.setColor(Color.GRAY.darker().darker());
			for (int k = 0; k <= 800; k++)
				for (int i = k%2; i<= 500; i+=2)
					g.drawLine(i,k,i,k);
		}
	}
	private void drawGameScreen(Graphics g)
	{
		if (System.currentTimeMillis() >= deadTime+1000)
		{
			if (nextLevel)
			{
				init();
			}
			//grid
/*			g.setColor(Color.BLACK);
			for (int k = 0; k <= 700; k += 25)
			{
				g.drawLine(0,k,700,k);
				g.drawLine(k,0,k,700);
			}
*/
			update();
			
			if (tryNorth && north != 'w' && north != 'd')
			{
				tryNorth = false;
				goNorth = true;
				goSouth = goEast = goWest = false;
			}
			if (trySouth && south != 'w' && south != 'd')
			{
				trySouth = false;
				goSouth = true;
				goNorth = goEast = goWest = false;
			}
			if (tryWest && west != 'w' && west != 'd')
			{
				tryWest = false;
				goWest = true;
				goSouth = goNorth = goEast = false;
			}
			if (tryEast && east != 'w' && east != 'd')
			{
				tryEast = false;
				goEast = true;
				goSouth = goNorth = goWest = false;
			}
			
			if ((int)((System.currentTimeMillis() - startTime)/1000)%25 == 0 && !isCherry)
			{
				ArrayList<Location> locs = getEmptyLocations();
				if (locs.size() > 0)
				{
					Location loc = locs.get((int)(Math.random()*locs.size()));
					
					grid[loc.getX()][loc.getY()] = 'C';
					isCherry = true;
				}
			}
			
			if (System.currentTimeMillis() >= lastMoved+MOVE_DELAY)
			{
				if (goNorth)
					goNorth();
				else if (goSouth)
					goSouth();
				else if (goWest)
					goWest();
				else if (goEast)
					goEast();
			}
		}
		//fill all squares
		fill(g);
		//pac
		if (System.currentTimeMillis() >= deadTime+1000)
			drawPac(g);
		else
			drawDeadPac(g);
	
		//score
		if (y >= 21)
			score++;
		
		g.setColor(Color.WHITE);
		g.setFont(new Font("OCR A Extended",Font.PLAIN,50));
		g.drawString("Score: "+score,25,565);
	
		//lives
		g.drawString("Lives: "+lives,25,605);
	
		//level
		g.setFont(new Font("OCR A Extended",Font.PLAIN,25));
		g.drawString("Level: "+level,25,635);
	
		//time
		g.drawString("Time: "+(System.currentTimeMillis()-startTime)/1000,25,665);
		g.setColor(Color.GREEN);
		if (specialDelay() > 0)
			g.drawString("Hit space for a laser!",75,720);
		
		//ghosts
		for (Ghost a : ghosts)
		{
			g.drawImage(a.getPic(),a.getX()*25,a.getY()*25,null);
			if (System.currentTimeMillis() >= a.getSpawnTime() + a.getConstantSpawnTime() + 1000)
			{
				a.move(x,y,direction);
				a.update();
			}
		}
		
		//laser
		if (laser)
		{
			if (goNorth)
			{
				int k = y;
				while(grid[k][x] != 'w' && grid[k][x] != '1' && grid[k][x] != '2')
					k--;
				g.setColor(Color.GREEN);
				g.fillOval(x*25+5,(k+1)*25+5,15,(y-k-1)*25+5);
				
				maxReach = new Location(x,k+1,0);
			}
			if (goSouth)
			{
				int k = y;
				while(grid[k][x] != 'w' && grid[k][x] != '1' && grid[k][x] != '2')
					k++;
				g.setColor(Color.GREEN);
				g.fillOval(x*25+5,(y+1)*25+5,15,(k-y-1)*25-5);
				
				maxReach = new Location(x,k-1,2);
			}
			if (goWest)
			{
				int k = x;
				while (grid[y][k] != 'w' && grid[y][k] != '1' && grid[y][k] != '2')
					k--;
				g.setColor(Color.GREEN);
				g.fillOval((k+1)*25+5,y*25+5,(x-k-1)*25+5,15);
				
				maxReach = new Location(k+1,y,3);
			}
			if (goEast)
			{
				int k = x;
				while (grid[y][k] != 'w' && grid[y][k] != '1' && grid[y][k] != '2')
					k++;
				g.setColor(Color.GREEN);
				g.fillOval((x+1)*25+5,y*25+5,(k-x-1)*25-5,15);
				
				maxReach = new Location(k-1,y,1);
			}
		}	
		
		//quit button
		
		//pointer on screen
		PointerInfo loc = MouseInfo.getPointerInfo();
		int a = (int)(loc.getLocation().getX() - frameLoc.getX());
		int b = (int)(loc.getLocation().getY() - frameLoc.getY());
	
		if (a >= 300 && a <= 450 && b >= 625 && b <= 700)
		{
			frame.setCursor(new Cursor(Cursor.HAND_CURSOR));
			startColor = rand.darker();
		}
		else
		{
			frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			startColor = rand.darker().darker();
		}
		
		g.setColor(startColor);
		g.fillRect(300,600,150,75);
		g.setColor(rand.darker().darker());
		g.drawString("Credits",325,645);
		
		//overlapped (ghosts/pac)
		if(specialDelay() == 0)
			numEaten = 0;
		for (Ghost c : ghosts)
		{
			if(c.overLapped() && specialDelay() == 0)
			{
				deadPos = new Location(x,y);
				dead();
				setDirection(-1);
				steps = 0;
				deadTime = System.currentTimeMillis();
				try{playSFX(1);}catch(Exception e){}
			}
			else if(c.overLapped() && specialDelay() > 0)
			{
				numEaten++;
				deadIncrement(Pacman.numEaten);
				c.pos(c.getType());
//				Pacman.delay(1000);
			}
		}
	}
	private void drawCredits(Graphics g)
	{
		g.drawImage(credits,0,0,null);
		g.setColor(Color.YELLOW);
		//border
		g.fillRect(0,500,500,500);
		g.fillRect(0,0,500,25);
		g.fillRect(0,0,25,500);
		g.fillRect(450,0,25,500);
		//empty space
		g.setColor(Color.BLACK);
		g.fillRect(25,525,425,175);
		//text
		g.setFont(new Font("OCR A Extended",Font.PLAIN,40));
		g.setColor(Color.YELLOW);
		g.drawString("Made by:",30,560);
		g.drawString("2009-2010",30,690);
		g.setColor(Color.WHITE);
		g.drawString("Miccah Castorina",50,605);
		g.drawString("Bryan Williamson",50,645);
		
		g.setFont(new Font("OCR A Extended",Font.PLAIN,15));
		g.setColor(Color.BLACK);
		g.drawString("You wasted " + finalTime + " seconds with this game.",75,20);
		
	}

	public void mouseEntered(MouseEvent e)
	{
		dull = false;
	}
	public void mouseExited(MouseEvent e)
	{
		dull = true;
	}
	public void mouseReleased(MouseEvent e){}
	public void mousePressed(MouseEvent e){}
	public void mouseClicked(MouseEvent e)
	{
		if (page == 1)
		{
			if (startColor.equals(Color.CYAN))
			{
				frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				page = 2;
				startTime = System.currentTimeMillis();
			}
		}
		else
		{
			if (startColor.equals(rand.darker()))
			{
				frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				finalTime = (int)((System.currentTimeMillis()-startTime)/1000);
				page = 3;
				stop();
				try{play(3);}catch(Exception b){}
			}
		}
	}

	private void update()
	{
		curPos = grid[y][x];
		north = grid[(y-1)][x];
		south = grid[(y+1)][x];
		west = grid[y][(x-1)];
		east = grid[y][(x+1)];
		if (goNorth)
			direction = 0;
		else if (goEast)
			direction = 1;
		else if (goSouth)
			direction = 2;
		else if (goWest)
			direction = 3;
	}

	private Color color(char a)
	{
		switch (a)
		{
			case 'w' : return rand;
			case 'e' : return rand;
			case ' ' : return Color.BLACK;
			case '.' : return Color.YELLOW;
			case 's' : return Color.ORANGE;
			case 'd' : return Color.RED;
			default  : return Color.BLACK.darker();
		}
	}

	public void keyPressed(KeyEvent e)
	{
		if (page == 2)
		{
			int code = e.getKeyCode();
			update();

			if (code == KeyEvent.VK_UP && (System.currentTimeMillis()-deadTime) > 1000)
			{
				tryNorth = true;
				trySouth = tryWest = tryEast = false;
			}
			if (code == KeyEvent.VK_DOWN && (System.currentTimeMillis()-deadTime) > 1000)
			{
				trySouth = true;
				tryNorth = tryWest = tryEast = false;
			}
			if (code == KeyEvent.VK_LEFT && (System.currentTimeMillis()-deadTime) > 1000)
			{
				tryWest = true;
				tryNorth = trySouth = tryEast = false;
			}
			if (code == KeyEvent.VK_RIGHT && (System.currentTimeMillis()-deadTime) > 1000)
			{
				tryEast = true;
				tryNorth = trySouth = tryWest = false;
			}
			
			if (code == KeyEvent.VK_SPACE && specialDelay() > 0)
				laser = true;

			update();
		}
	}
	public void keyReleased(KeyEvent e)
	{
		int code = e.getKeyCode();
		if (code == KeyEvent.VK_SPACE)
			laser = false;
	}
	public void keyTyped(KeyEvent e){}

	private void convert(String s)
	{
		String [] temp = s.split(" ");
		y = Integer.valueOf(temp[0]);
		x = Integer.valueOf(temp[1]);
	}

	private void goNorth()
	{
		if (north != 'w' && north != 'd')
		{
			if (north == '1')
				convert(tele2);
			if (north == '2')
				convert(tele1);
			y--;
			update();
			if (curPos == '.')
			{
				grid[y][x] = ' ';
				eaten++;
				score+=SCORE_INCREMENT;
			}
			if (curPos == 'C')
			{
				grid[y][x] = ' ';
				score+=250;
				isCherry = false;
			}
			if (curPos == 's')
			{
				grid[y][x] = ' ';
				
				valid = true;
				MOVE_DELAY = Ghost.DELAY-50;
				timeEaten = System.currentTimeMillis();
				specialDelay();

				score+=SPECIAL_INCREMENT;
				eaten++;
			}

			steps++;
			update();
			if (goNorth)
				lastMoved(System.currentTimeMillis());
		}
	}
	private void goSouth()
	{
		if (south != 'w' && south != 'd')
		{
			if (south == '1')
				convert(tele2);
			if (south == '2')
				convert(tele1);
			y++;
			update();
			if (curPos == '.')
			{
				grid[y][x] = ' ';
				eaten++;
				score+=SCORE_INCREMENT;
			}
			if (curPos == 'C')
			{
				grid[y][x] = ' ';
				score+=250;
				isCherry = false;
			}
			if (curPos == 's')
			{
				grid[y][x] = ' ';
				
				valid = true;
				MOVE_DELAY = Ghost.DELAY-50;
				timeEaten = System.currentTimeMillis();
				specialDelay();

				score+=SPECIAL_INCREMENT;
				eaten++;
			}

			steps++;
			update();
			if (goSouth)
				lastMoved(System.currentTimeMillis());
		}
	}
	private void goWest()
	{
		if (west != 'w' && west != 'd')
		{
			if (west == '1')
				convert(tele2);
			if (west == '2')
				convert(tele1);
			x--;
			update();
			if (curPos == '.')
			{
				grid[y][x] = ' ';
				eaten++;
				score+=SCORE_INCREMENT;
			}
			if (curPos == 'C')
			{
				grid[y][x] = ' ';
				score+=250;
				isCherry = false;
			}
			if (curPos == 's')
			{
				grid[y][x] = ' ';
				
				valid = true;
				MOVE_DELAY = Ghost.DELAY-50;
				timeEaten = System.currentTimeMillis();
				specialDelay();

				score+=SPECIAL_INCREMENT;
				eaten++;
			}

			steps++;
			update();
			if (goWest)
				lastMoved(System.currentTimeMillis());
		}
	}
	private void goEast()
	{
		if (east != 'w' && east != 'd')
		{
			if (east == '1')
				convert(tele2);
			if (east == '2')
				convert(tele1);
			x++;
			update();
			if (curPos == '.')
			{
				grid[y][x] = 'o';
				eaten++;
				score+=SCORE_INCREMENT;
			}
			if (curPos == 'C')
			{
				grid[y][x] = ' ';
				score+=250;
				isCherry = false;
			}
			if (curPos == 's')
			{
				grid[y][x] = 'o';
				
				valid = true;
				MOVE_DELAY = Ghost.DELAY-50;
				timeEaten = System.currentTimeMillis();
				specialDelay();

				score+=SPECIAL_INCREMENT;
				eaten++;
			}

			steps++;
			update();
			if (goEast)
				lastMoved(System.currentTimeMillis());
		}
	}

	private void lastMoved(long a)
	{
		lastMoved = a;
	}
	public static int specialDelay()
	{
		if (timeEaten >= System.currentTimeMillis()-SPECIAL_DELAY * 3 / 5 && valid)
			return 2;
		if (timeEaten >= System.currentTimeMillis()-SPECIAL_DELAY && valid)
			return 1;
		
		laser = false;
		MOVE_DELAY = Ghost.DELAY;
		return 0;
	}


	// MAIN METHOD

	public static void main(String...args) throws Exception
	{
		Pacman panel = new Pacman();
		frame = new JFrame("Jacman");
		frame.setSize(482,750);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//Prevent Resizing
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);

		frame.add(panel);
		frame.addKeyListener(panel);
		frame.addMouseListener(panel);

		frame.setVisible(true);
		frame.setBackground(Color.BLACK);

		frameLoc = frame.getLocation();

		//music and background
		play(1);
		pac = ImageIO.read(new File("PacGif.GIF"));
		cherry = ImageIO.read(new File("cherry.gif")).getScaledInstance(25,25,0);
		
		boolean beginning = true;
		boolean finished = false;
		while(!finished)
		{
			frameLoc = frame.getLocation();

			if (eaten >= pellets)
				nextLevel = true;
			Graphics g = frame.getGraphics();
			frame.repaint();
			try
			{
				Thread.sleep(1000/120);
			}
			catch(Exception e){}

			if (page == 2 && beginning)
			{
				stop();
				play(2);
				beginning = false;
			}
			if (page == 3 && !finished)
			{
				for (int k = 1; k <= 9; k++)
				{
					credits = ImageIO.read(new File("creditBackground"+k+".jpg")).getScaledInstance(500,500,0);
					frame.repaint();
					delay(3000);
				}
			}
		}
		stop();
	}
	
	public static void delay(long b)
	{
		long a = System.currentTimeMillis();
		while(a+b >= System.currentTimeMillis());
	}
	
	private static void play(int a) throws Exception
	{
		switch(a)
		{
			case 1 : stream = AudioSystem.getAudioInputStream(startFile); break;
			case 2 : stream = AudioSystem.getAudioInputStream(gameFile); break;
			case 3 : stream = AudioSystem.getAudioInputStream(creditFile); break;
			default: stream = AudioSystem.getAudioInputStream(startFile); break;
		}
		music = AudioSystem.getClip();
		
		music.open(stream);
		music.start();
		music.loop(Clip.LOOP_CONTINUOUSLY);
	}
	private static void playSFX(int a) throws Exception
	{
		switch(a)
		{
			case 1 : stream = AudioSystem.getAudioInputStream(pacDies); break;
		}
		musicFX = AudioSystem.getClip();

		musicFX.open(stream);
		musicFX.start();
	}
	
	private static void stop()
	{
		music.stop();
		music.close();
	}
	
	public static void setDirection(int n)
	{
		switch(n)
		{
			case 0 : goNorth = true; goSouth = goEast = goWest = false; break;
			case 1 : goEast = true; goSouth = goNorth = goWest = false; break;
			case 2 : goSouth = true; goNorth = goEast = goWest = false; break;
			case 3 : goWest = true; goSouth = goEast = goNorth = false; break;
			default: goNorth = goSouth = goEast = goWest = false;
		}
	}
	private static ArrayList<Location> getEmptyLocations()
	{
		ArrayList<Location> locs = new ArrayList<Location>();
		for (int k = 0; k < grid.length; k++)
			for (int i = 0; i < grid[k].length; i++)
				if (grid[k][i] == ' ')
					locs.add(new Location(k,i));
		return locs;
	}
}


class Ghost
{
	private int type;
	private int xPos,yPos,direction,steps;
	private char north,south,east,west,curPos;
	private long moveTime,spawnTime;
	private int scared;
	
	private Image pic;
	private Image myPic;
	private static Image ghostBlue,ghostWhite;
	
	private int targetX,targetY;

	private int pacX,pacY;
	private int pacDirection;

	public static final int DELAY = 175;
	
	private static final int NORTH = 0;
	private static final int EAST = 1;
	private static final int SOUTH = 2;
	private static final int WEST = 3;

	public static final int BLINKY = 1;
	public static final int PINKY = 2;
	public static final int INKY = 3;
	public static final int CLYDE = 4;
	
	public Ghost(int n)
	{
		steps = 0;
		try
		{
			switch(n)
			{
				case 1 : pic = ImageIO.read(new File("blinky.gif")); break;
				case 2 : pic = ImageIO.read(new File("pinky.gif")); break;
				case 3 : pic = ImageIO.read(new File("inky.gif")); break;
				case 4 : pic = ImageIO.read(new File("clyde.gif")); break;
			}
			myPic = pic;
			ghostBlue = ImageIO.read(new File("ghostBlue.gif"));
			ghostWhite = ImageIO.read(new File("ghostWhite.gif"));
		}
		catch(Exception e){}
		
		type = n;
		direction = 0;
		pos(type);
	}
	public void pos(int a)
	{
		spawnTime = System.currentTimeMillis();
		
		Location bS = Pacman.blinkySpawn;
		Location pS = Pacman.pinkySpawn;
		Location iS = Pacman.inkySpawn;
		Location cS = Pacman.clydeSpawn;
		
		switch (a)
		{
			case 1 : xPos = bS.getX(); yPos = bS.getY(); break;
			case 2 : xPos = pS.getX(); yPos = pS.getY(); break;
			case 3 : xPos = iS.getX(); yPos = iS.getY(); break;
			case 4 : xPos = cS.getX(); yPos = cS.getY(); break;
			default: break;
		}
		if (type == 4)
			type = (int)(Math.random()*3+1);
	}

	public void move(int pX, int pY, int d)
	{		
		if (System.currentTimeMillis() >= moveTime+DELAY)
		{
			pacDirection = d;
			pacX = pX;
			pacY = pY;
			
			switch(type)
			{
				case 1 :
					switch (pacDirection)
					{
						case NORTH : targetX = pacX; targetY = pacY - 2; break;
						case EAST : targetX = pacX + 2; targetY = pacY; break;
						case SOUTH : targetX = pacX; targetY = pacY + 2; break;
						case WEST : targetX = pacX - 2; targetY = pacY; break;
						default: break;
					}break;

				case 2 :
					switch (pacDirection)
					{
						case NORTH : targetX = pacX; targetY = pacY + 2; break;
						case EAST : targetX = pacX - 2; targetY = pacY; break;
						case SOUTH : targetX = pacX; targetY = pacY - 2; break;
						case WEST : targetX = pacX + 2; targetY = pacY; break;
						default: break;
					}break;
				default: break;
			}
			move(direction);
			lastMoveTime(System.currentTimeMillis());
		}
	}
	public int getX()
	{
		return xPos;
	}
	public int getY()
	{
		return yPos;
	}
	
	public void update()
	{
		north = Pacman.grid[(yPos-1)][xPos];
		south = Pacman.grid[(yPos+1)][xPos];
		west = Pacman.grid[yPos][(xPos-1)];
		east = Pacman.grid[yPos][(xPos+1)];
		curPos = Pacman.grid[yPos][xPos];
		scared = Pacman.specialDelay();
		
		if (scared == 2)
			pic = ghostBlue;
		else if (scared == 1)
		{
			if (steps%2 == 0)
				pic = ghostWhite;
			else
				pic = ghostBlue;
		}
		else
			pic = myPic;
			
		pacX = Pacman.x;
		pacY = Pacman.y;
	}
	public Image getPic()
	{
		return pic;
	}
	private void lastMoveTime(long a)
	{
		moveTime = a;
	}

	private void move(int dir)
	{
		update();
		steps++;
		
		
		ArrayList<Location> list = getEmptyLocations();

		if (west == '1' && dir == WEST)
			xPos = 18;
		else if (east == '2' && dir == EAST)
			xPos = 0;
		
		else
			if (type == 3)
			{
				if (list.size() > 0)
				{
					int rand = (int)(Math.random()*list.size());
					direction = list.get(rand).getDirection();
				}
				else
					direction = (dir+2)%4;
			}
		if (type != 3)
		{
			if (north == 'd' || curPos == 'd')
				direction = NORTH;
			else if (south == 'd' && direction == NORTH)
			{
				if (Math.random() > 0.5 && west != 'w')
					direction = WEST;
				else if (east != 'w')
					direction = EAST;
			}
			else
			{
				if (list.size() > 1)
				{
					//AI
					int a,b;
					if (Pacman.level >= 25)
					{
						a = xPos - pacX;
						b = yPos - pacY;
					}
					else
					{
						a = xPos - targetX;
						b = yPos - targetY;
					}
					
					if (Math.random() > difficulty())
					{
						if (scared == 0)
						{
							if (b < 0 && south != 'w' && direction != NORTH)
								direction = SOUTH;
							else if (b > 0 && north != 'w' && direction != SOUTH)
								direction = NORTH;
							else if (a > 0 && west != 'w' && direction != EAST)
								direction = WEST;
							else if (a < 0 && east != 'w' && direction != WEST)
								direction = EAST;
							
							else if (a == 0 || b == 0)
							{
								int rand = (int)(Math.random()*list.size());
								direction = list.get(rand).getDirection();
							}
						}
						else
						{
							if (b > 0 && south != 'w' && direction != NORTH)
								direction = SOUTH;
							else if (b < 0 && north != 'w' && direction != SOUTH)
								direction = NORTH;
							else if (a < 0 && west != 'w' && direction != EAST)
								direction = WEST;
							else if (a > 0 && east != 'w' && direction != WEST)
								direction = EAST;
							
							else if (a == 0 || b == 0)
							{
								int rand = (int)(Math.random()*list.size());
								direction = list.get(rand).getDirection();
							}
						}
					}
					else
						direction = list.get((int)(Math.random()*list.size())).getDirection();
				}
				else if (list.size() == 1)
					direction = list.get(0).getDirection();
				else
					direction = (dir+2)%4;
			}
		}
		switch (direction)
		{
			case NORTH : yPos--; break;
			case EAST : xPos++; break;
			case SOUTH : yPos++; break;
			case WEST : xPos--; break;
			default : break;
		}
		update();
	}

	private ArrayList<Location> getEmptyLocations()
	{
		ArrayList<Location> list = new ArrayList<Location>();
		update();
		if (north != 'w' && north != 'e' && direction != SOUTH)
			list.add(new Location(xPos,yPos-1,0));
		if (south != 'w' && south != 'd' && south != 'e' && direction != NORTH)
			list.add(new Location(xPos,yPos+1,2));
		if (east != 'w' && east != 'e' && direction != WEST)
			list.add(new Location(xPos+1,yPos,1));
		if (west != 'w' && west != 'e' && direction != EAST)
			list.add(new Location(xPos-1,yPos,3));
		return list;
	}
	
	public long getConstantSpawnTime()
	{
		switch (type)
		{
			case BLINKY : return 1000;
			case PINKY : return 2000;
			case INKY : return 3000;
			case CLYDE : return 4000;
			default : return 0;
		}
	}
	public long getSpawnTime()
	{
		return spawnTime;
	}
	public boolean overLapped()
	{
		update();

		if(pacX == xPos && pacY == yPos)
			return true;
		if (Pacman.laser)
		{
			if (pacDirection == NORTH && xPos == pacX && yPos >= Pacman.maxReach.getY())
				return true;
			if (pacDirection == SOUTH && xPos == pacX && yPos <= Pacman.maxReach.getY())
				return true;
			if (pacDirection == EAST && yPos == pacY && xPos <= Pacman.maxReach.getX())
				return true;
			if (pacDirection == WEST && yPos == pacY && xPos >= Pacman.maxReach.getX())
				return true;
		}
		return false;
	}
	private double difficulty()
	{
		switch (Pacman.level)
		{
			case 1 : return 0.75;
			case 2 : return 0.66;
			case 3 : return 0.5;
			case 4 : return 0.33;
			case 5 : return 0.33;
			case 6 : return 0.25;
			case 7 : return 0.25;
			case 8 : return 0.1;
			case 9 : return 0.1;
			case 10: return 0.1;
			default: return 0;
		}
	}
	public int getType()
	{
		return type;
	}
}


class Location
{
	private int x,y,dir;
	public Location(int a, int b)
	{
		x = a;
		y = b;
		dir = 0;
	}
	public Location(int a, int b, int d)
	{
		x = a;
		y = b;
		dir = d;
	}
	public int getX()
	{
		return x;
	}
	public int getY()
	{
		return y;
	}
	public int getDirection()
	{
		return dir;
	}
}	//1337
