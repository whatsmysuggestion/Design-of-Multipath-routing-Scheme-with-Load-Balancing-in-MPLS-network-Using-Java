

import java.util.*;
import java.awt.*;
import java.applet.Applet;
import java.awt.event.*;


class Node2 {
    double x;
    double y;

    double dx;
    double dy;

    boolean fixed;

    String lbl;
}


class Edge2 {
    int from;
    int to;
    Color color = Color.black;
	
    double len;
	int w, s;
}

class Flow2 {
    int from;
    int to;
	
	int A[], B[];
    int hopA, hopB;
}

class GraphPanel2 extends Panel
    implements Runnable, MouseListener, MouseMotionListener {
    Graph2 graph;
    int nnodes;
    Node2 nodes[] = new Node2[60];

    int nedges;
    Edge2 edges[] = new Edge2[100];

	int nflows = 0;
	Flow2 flows[] = new Flow2[3540]; // =60*59
	int C[][] = new int[200][200];
	int dmdm[][] = new int[60][60];
	
    Thread relaxer;
    boolean stress;
    boolean random;

    GraphPanel2(Graph2 graph) {
	this.graph = graph;
	addMouseListener(this);
    }

    int findNode(String lbl) {
       // System.out.print(lbl);
	for (int i = 0 ; i < nnodes ; i++) {
		if (nodes[i]!=null){
			if (nodes[i].lbl.equals(lbl)) {
				return i;
			}
		}
	}
	return addNode(lbl);
    }
    int addNode(String lbl) {
	Node2 n = new Node2();
	n.x = 10 + 380*Math.random();
	n.y = 10 + 380*Math.random();
	n.lbl = lbl;
	nodes[nnodes] = n;
	return nnodes++;
    }
    void addEdge(String from, String to, int len) {
		Edge2 e = new Edge2();
		e.from = findNode(from);
		e.to = findNode(to);
		e.len = len;
		edges[nedges++] = e;
    }

	void addFlow(int from, int to, String str1, String str2) {
		Flow2 f = new Flow2();
		f.A = new int[nedges];
		f.B = new int[nedges];
		f.from = from; 
		f.to = to;
                //System.out.println("nflows="+nflows);
		dmdm[from][to] = nflows;
		dmdm[to][from] = nflows;
		for (int k=0; k<nedges; k++){
			f.A[k] = Integer.valueOf(str1.substring(k,k+1)).intValue(); 
                       // System.out.println("A["+k+"]="+f.A[k]);
			f.hopA += f.A[k];
			f.B[k] = Integer.valueOf(str2.substring(k,k+1)).intValue(); 
                        //System.out.print("B["+k+"]="+f.B[k]);
			f.hopB += f.B[k];
		}
		flows[nflows] = f;
		nflows ++;
	}
	
	void showFlow(Node2 n1, Node2 n2) {
		int i1 = findNode(n1.lbl);
		int i2 = findNode(n2.lbl);
                
		int fn = dmdm[i1][i2];
		Flow2 f = flows[fn];
               //Flow f=new Flow(fn);
                //System.out.println(nedges);
		for (int i = 0 ; i < nedges ; i++) {
                  //System.out.println("in showFlow A[i]="+f.A[i]);
	   	   	  if (f.A[i]==1 )
				edges[i].color = workColor;
			else if (f.B[i]==1)
				edges[i].color = backupColor;
			else
				edges[i].color = arcColor1;
		}
		offgraphics.drawImage(offscreen, 0, 0, null);
                 double m=Math.random()/10;
               String s=Double.toString(m);
               String s1=s.substring(0,6);
                graph.t1.setText(s1);
                String s2=s1.substring(4,5);
                int leng=Integer.parseInt(s2);
                leng=leng*50;
                graph.t2.setText(""+leng);
	}
	
	
	public void run() {
        Thread me = Thread.currentThread();
	while (relaxer == me) {
	    relax();
	    if (random && (Math.random() < 0.03)) {
		Node2 n = nodes[(int)(Math.random() * nnodes)];
		if (!n.fixed) {
		    n.x += 100*Math.random() - 50;
		    n.y += 100*Math.random() - 50;
		}
		//graph.play(graph.getCodeBase(), "audio/drip.au");
	    }
	    try {
		Thread.sleep(100);
	    } catch (InterruptedException e) {
		break;
	    }
	}
    }

    synchronized void relax() {
	for (int i = 0 ; i < nedges ; i++) {
	    Edge2 e = edges[i];
	    double vx = nodes[e.to].x - nodes[e.from].x;
	    double vy = nodes[e.to].y - nodes[e.from].y;
	    double len = Math.sqrt(vx * vx + vy * vy);
            len = (len == 0) ? .0001 : len;
	    double f = (edges[i].len - len) / (len * 3);
	    double dx = f * vx;
	    double dy = f * vy;

	    nodes[e.to].dx += dx;
	    nodes[e.to].dy += dy;
	    nodes[e.from].dx += -dx;
	    nodes[e.from].dy += -dy;
	}

	for (int i = 0 ; i < nnodes ; i++) {
	    Node2 n1 = nodes[i];
	    double dx = 0;
	    double dy = 0;

	    for (int j = 0 ; j < nnodes ; j++) {
		if (i == j) {
		    continue;
		}
		Node2 n2 = nodes[j];
		double vx = n1.x - n2.x;
		double vy = n1.y - n2.y;
		double len = vx * vx + vy * vy;
		if (len == 0) {
		    dx += Math.random();
		    dy += Math.random();
		} else if (len < 100*100) {
		    dx += vx / len;
		    dy += vy / len;
		}
	    }
	    double dlen = dx * dx + dy * dy;
	    if (dlen > 0) {
		dlen = Math.sqrt(dlen) / 2;
		n1.dx += dx / dlen;
		n1.dy += dy / dlen;
	    }
	}

	Dimension d = getSize();
	for (int i = 0 ; i < nnodes ; i++) {
	    Node2 n = nodes[i];
	    if (!n.fixed) {
		n.x += Math.max(-5, Math.min(5, n.dx));
		n.y += Math.max(-5, Math.min(5, n.dy));
            }
            if (n.x < 0) {
                n.x = 0;
            } else if (n.x > d.width) {
                n.x = d.width;
            }
            if (n.y < 0) {
                n.y = 0;
            } else if (n.y > d.height) {
                n.y = d.height;
            }
	    n.dx /= 2;
	    n.dy /= 2;
	}
	repaint();
    }

    Node2 pick;
    boolean pickfixed;
    Image offscreen;
    Dimension offscreensize;
    Graphics offgraphics;

    final Color fixedColor = Color.white;
    final Color selectColor = Color.pink;
    final Color edgeColor = Color.black;
    final Color nodeColor = new Color(120, 150, 200);
    final Color stressColor = new Color(120, 150, 200);
    final Color arcColor1 = Color.black;
    final Color arcColor2 = Color.pink;
    final Color arcColor3 = Color.red;
    final Color workColor = Color.green;
    final Color backupColor = Color.red; 
   
    public void paintNode(Graphics g, Node2 n, FontMetrics fm) {
		int x = (int)n.x;
		int y = (int)n.y;
		g.setColor((n == pick) ? selectColor : (n.fixed ? fixedColor : nodeColor));
		int w = fm.stringWidth(n.lbl) + 10;
		int h = fm.getHeight() + 4;
		g.fillRect(x - w/2, y - h / 2, w, h);
		g.setColor(Color.black);
		g.drawRect(x - w/2, y - h / 2, w-1, h-1);
		g.drawString(n.lbl, x - (w-10)/2, (y - (h-4)/2) + fm.getAscent());
                
                
    }

    public synchronized void update(Graphics g) {
		Dimension d = getSize();
		if ((offscreen == null) || (d.width != offscreensize.width) || (d.height != offscreensize.height)) {
		    offscreen = createImage(d.width, d.height);
		    offscreensize = d;
			if (offgraphics != null) {
				offgraphics.dispose();
			}
			offgraphics = offscreen.getGraphics();
			offgraphics.setFont(getFont());
		}

		offgraphics.setColor(getBackground());
		
                offgraphics.fillRect(0, 0, d.width, d.height);
		for (int i = 0 ; i < nedges ; i++) {
		    Edge2 e = edges[i];
		    int x1 = (int)nodes[e.from].x;
		    int y1 = (int)nodes[e.from].y;
		    int x2 = (int)nodes[e.to].x;
			int y2 = (int)nodes[e.to].y;
		    int len = (int)Math.abs(Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2)) - e.len);
			offgraphics.setColor(e.color) ;
                        
                        
                        
			offgraphics.drawLine(x1, y1, x2, y2);
			if (stress) {
			String lbl = String.valueOf(len);
			offgraphics.setColor(stressColor);
			offgraphics.drawString(lbl, x1 + (x2-x1)/2, y1 + (y2-y1)/2);
			offgraphics.setColor(edgeColor);
			}
		}
	
		FontMetrics fm = offgraphics.getFontMetrics();
		for (int i = 0 ; i < nnodes ; i++) {
			paintNode(offgraphics, nodes[i], fm);
		}
		g.drawImage(offscreen, 0, 0, null);
	}

    //1.1 event handling
    public void mouseClicked(MouseEvent e) {
       
       
		if (graph.topo)	{
		}
		else{
		double bestdist = Double.MAX_VALUE;
		int x = e.getX();
		int y = e.getY();
		for (int i = 0 ; i < nnodes ; i++) {
			Node2 n = nodes[i];
			double dist = (n.x - x) * (n.x - x) + (n.y - y) * (n.y - y);
			if (dist < bestdist) {
			pick = n;
			bestdist = dist;
			}
		}
		pickfixed = pick.fixed;
		pick.fixed = ! pick.fixed;
		pick.x = x;
		pick.y = y;
                repaint();
                
		e.consume();
		}
    
    }

    public void mousePressed(MouseEvent e) {
        
        
             addMouseMotionListener(this);
	        	if (graph.topo)	{
            
			                  double bestdist = Double.MAX_VALUE;
			                  int x = e.getX();
			                  int y = e.getY();
                                          //System.out.println("value for pick"+pick);
			                   if (pick != null)
                                            {
			                	Node2 p2 = pick;
				                for (int i = 0 ; i < nnodes ; i++) 
                                                 {
					            Node2 n = nodes[i];
					             double dist = (n.x - x) * (n.x - x) + (n.y - y) * (n.y - y);
					              if (dist < bestdist) 
                                                       {
					          	p2 = n;
						        bestdist = dist;
					               }
				                  }
				
                                                if (pick != p2)
                                                showFlow(pick, p2);
				                else 
					        pick = null;
			                       } 
                                           else
                                             {
				               for (int i = 0 ; i < nnodes ; i++)
                                                {
					         Node2 n = nodes[i];
					         double dist = (n.x - x) * (n.x - x) + (n.y - y) * (n.y - y);
					          if (dist < bestdist)
                                                  {
						   pick = n;
						   bestdist = dist;
					           }
				                 }
			                        }
			
		                     }
                                    else
                                    {
	                             double bestdist = Double.MAX_VALUE;
	                             int x = e.getX();
	                             int y = e.getY();
	                             for (int i = 0 ; i < nnodes ; i++)
                                     {
	                               Node2 n = nodes[i];
	                                double dist = (n.x - x) * (n.x - x) + (n.y - y) * (n.y - y);
	                                 if (dist < bestdist)
                                          {
		                           pick = n;
		                           bestdist = dist;
	                                   }
	                               }
	                              pickfixed = pick.fixed;
	                              pick.fixed = true;
	                              pick.x = x;
	                              pick.y = y;
	                              repaint();
	                              e.consume();
	                        }
        
    
    
    }
    public void mouseReleased(MouseEvent e) {
   
        removeMouseMotionListener(this);
		if (graph.topo)	{
		}
		else {
        if (pick != null) {
            pick.x = e.getX();
            pick.y = e.getY();
            pick.fixed = pickfixed;
            if (!graph.topo)
				pick = null;
        }
	repaint();
	e.consume();
		}
  
        }

	public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
		if (graph.topo){
		}else {
			pick.x = e.getX();
			pick.y = e.getY();
			repaint();
			e.consume();
		}	
	}

    public void mouseMoved(MouseEvent e) {
    }

    public void start() {
	relaxer = new Thread(this);
	relaxer.start();
    }

    public void stop() {
	relaxer = null;
    }

}


public class Graph2 extends Applet implements ActionListener, ItemListener {
    

    GraphPanel2 panel;
    Panel controlPanel;

    Button state = new Button(" Topology ");
    Button scramble = new Button("Scramble");
    Button shake = new Button("Shake");
    Checkbox stress = new Checkbox("Stress");
    Checkbox random = new Checkbox("Random");
    Label failure=new Label("Dual failure");
    Label backup=new Label("Backup Path");
    Label Time=new Label("TIS");
    TextField t1=new TextField("       ");
    Label BPLength=new Label("BPLength");
    TextField t2=new TextField("      ");
   

	boolean topo = false;

   
	
    public void init(){
        try{
               setBackground(Color.DARK_GRAY);  
		setLayout(new BorderLayout());

		panel = new GraphPanel2(this);
		add("Center", panel);
		controlPanel = new Panel();
		add("North", controlPanel);

		controlPanel.add(state); state.addActionListener(this);
                state.setBackground(new Color(52,52,22));state.setForeground(new Color(50, 120, 100));
		controlPanel.add(scramble); scramble.addActionListener(this);
                scramble.setBackground(new Color(52,52,22));scramble.setForeground(new Color(50, 120, 100));
		controlPanel.add(shake); shake.addActionListener(this);
                shake.setBackground(new Color(52,52,22));shake.setForeground(new Color(50, 120, 100));
		controlPanel.add(stress); stress.addItemListener(this);
                stress.setForeground(new Color(50, 120, 100));
	        controlPanel.add(random); random.addItemListener(this);
                random.setForeground(new Color(50, 120, 100));
	        controlPanel.add(random); random.addItemListener(this);
                random.setForeground(new Color(0,0,255));
                  controlPanel.add(failure); random.addItemListener(this);
                  controlPanel.add(backup); random.addItemListener(this);
                failure.setForeground(Color.RED);
                backup.setForeground(Color.GREEN);
                controlPanel.add(Time);Time.setForeground(new Color(120, 150, 200));
                controlPanel.add(t1);
                t1.setSize(20,25);
                t1.setEditable(false);
                controlPanel.add(BPLength);BPLength.setForeground(new Color(120, 150, 200));
                controlPanel.add(t2); 
                t2.setSize(20,25);
                t2.setEditable(false);
              
		
		String net ="16";//getParameter("");
		if (net != null){
			int nn = Integer.valueOf(net).intValue();
			for (int i =0; i < nn; i++) 
				panel.addNode(Integer.toString(i+1));
		}
			//"17/30:30,9/20:70,16/60:80,2/220:340,11/220:30"
	       String fixed ="";// getParameter("fixed");
		 if (fixed != null){
		 for (StringTokenizer t = new StringTokenizer(fixed, ",") ;t.hasMoreTokens() ;)
                 {
		   String str = t.nextToken();
		   int i = str.indexOf('/');
		   if (i > 0) 
                      {
			Node2 n = panel.nodes[panel.findNode(str.substring(0,i))];
			str = str.substring(i+1);
			n.fixed = true;
			int j = str.indexOf(':');
			if (j > 0)
                        {
			n.x = Integer.valueOf(str.substring(0,j)).intValue();
			str = str.substring(j+1);
			n.y = Integer.valueOf(str).intValue();
			}
		     }
		}
		}
               //"1-2/80,1-3/80,1-4/80,2-3/80,2-4/80,3-4/80"
               //'C-N1,C-N2,C-N3,C-NX,N1-N2/M12,N2-N3/M23,N3-NX/M3X,...'
		//"1-8/10,1-11/40,1-13/80,1-3/44,2-14/4,2-12/14,2-18/00,3-17/32,3-16/22,3-5/10,3-13/14,4-7/12,4-15/16,4-18/10,5-3/10,5-16/12,5-15/4,6-8/14,6-11/6,7-9/12,9-17/16,10-11/26,10-14/30,10-12/30,12-18/38,13-14/12,14-15/28,16-17/16";
		String edges ="1-2/20,1-4/20,1-5/20,1-13/80,2-3/20,2-6/20,2-14/80,3-4/20,3-7/20,3-15/80,4-8/20,4-16/80,5-6/20,5-9/20,5-8/80,6-7/20,6-10/20,7-8/20,7-11/20,8-12/20,9-10/20,9-12/80,9-13/20,10-11/20,10-14/20,11-12/20,11-15/20,12-16/20,13-14/20,13-16/80,14-15/20,15-16/20";//getParameter("Edge-one/Edge-two");
		for (StringTokenizer t = new StringTokenizer(edges, ",");t.hasMoreTokens() ; ) {
			String str = t.nextToken();
			int i = str.indexOf('-');
			if (i > 0) 
                        {
			int len = 50;
			int j = str.indexOf('/');
                        
			if (j > 0) 
                        {
			    len = Integer.valueOf(str.substring(j+1)).intValue();
			    str = str.substring(0, j);
			    }
			panel.addEdge(str.substring(0,i), str.substring(i+1), len);
			}
	    }	
	
		Dimension d = getSize();
		String center ="";
                
		/*if (center != null){
		    Node n = panel.nodes[panel.findNode(center)];
		    n.x = d.width / 2;
		    n.y = d.height / 2;
		    n.fixed = true;
		}*/
                //0 0 0 0 0 , 0 0 0 0 0 , 0  0  0 0  0 , 0 0  0  0  0 , 0 0  0  0  0 , 0 0  0  0  0 , 0  0
	//        1 2 3 4 5   6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30  31 32  
                //     1-200101000000000000000000000000100     ;1-310000000000000000000000000000;1-4000000000000000000000001000;1-5001010000000000000000000000000;1-600101000000000000000000000000;1-700101000000000000000000000000;1-80010100000000000000000000000;1-900101000000000000000000000000;1-1000101000000000000000000000000;1-110001010000000000000000001000; 1-12 10100000000000000000000000;1-13 010000000000000000000000010;1-14010000000000000000000000010;1-1501000000000000000000000001000;1-16001000000000000000000000001;2-3101000000000000000000000001000;2-4101000000000000000000000001000;2-5000000000000000000000001000;2-611010000000000000000000001000;2-710100000000000000000000001000;2-801000000000000000000000001000;2-941010000000000000000000001000;2-10 1000000000000000000000001000;2-11 01000000000000000000001000;2-12 000000000000000000000001000;2-13 000000000000000000000001000;2-14 000000000000000000000001000;2-15 000000000000000000000001000;2-16 000000000000000000000001000;3 -4101000000000000000000000001000;3-5000000000000000000000001000;3-611010000000000000000000001000;3-710100000000000000000000001000;3-801000000000000000000000001000;3-941010000000000000000000001000;3-10 1000000000000000000000001000;3-11 01000000000000000000001000;3-12 000000000000000000000001000;3-13 000000000000000000000001000;3-14 000000000000000000000001000;3-15 000000000000000000000001000;3-16 000000000000000000000001000;4-5000000000000000000000001000   ;4-611010000000000000000000001000;4-710100000000000000000000001000;4-801000000000000000000000001000;4-941010000000000000000000001000;4-10 1000000000000000000000001000;4-11 01000000000000000000001000;4-12 000000000000000000000001000;4-13 000000000000000000000001000;4-14 000000000000000000000001000;4-15 000000000000000000000001000;4-16 000000000000000000000001000;5-611010000000000000000000001000;5-710100000000000000000000001000;5-801000000000000000000000001000;5-941010000000000000000000001000;5-10 1000000000000000000000001000;5-11 01000000000000000000001000;5-12 00000 00000 00000 00000 00010 00;5-13 0000000000000000001000;5-14 000000000000000000000001000;5-15 0000000000000000000000000000;5-16 000000000000000000000001000;6-710100000000000000000000001000;6-801000000000000000000000001000;6-941010000000000000000000001000;6-10 1000000000000000000000001000;6-11 01000000000000000000001000;6-12 000000000000000000000001000;6-13 000000000000000000000001000;6-14 000000000000000000000001000;6-15 000000000000000000000001000;6-16 000000000000000000000001000;7-801000000000000000000000001000;7-941010000000000000000000001000;7-10 1000000000000000000000001000;7-11 01000000000000000000001000;7-12 000000000000000000000001000;7-13 000000000000000000000001000;7-14 000000000000000000000001000;7-15 000000000000000000000001000;7-16 000000000000000000000001000;8-941010000000000000000000001000;8-10 1000000000000000000000001000;8-11 01000000000000000000001000;8-12 000000000000000000000001000;8-13 000000000000000000000001000;8-14 000000000000000000000001000;8-15 000000000000000000000001000;8-16 000000000000000000000001000;9-10 1000000000000000000000001000;9-11 01000000000000000000001000;9-12 000000000000000000000001000;9-13 000000000000000000000001000;9-14 000000000000000000000001000;9-15 000000000000000000000001000;9-16 0000000000000000000000010;10-11 010000000000000000000010;10-12 000000000000000000000001000;10-13 0000000000000000000001000;10-14 0000000000000000000001000;10-15 000000000000000000000001000;10-16 000000000000000000000001000;11-12 000000000000000000000001000;11-13 0000000000000000000001000;11-14 0000000000000000000001000;11-15 000000000000000000000001000;11-16 000000000000000000000001000;12-13 0000000000000000000001000;12-14 0000000000000000000001000;12-15 000000000000000000000001000;12-16 000000000000000000000001000;13-14 0000000000000000000001000;13-15 000000000000000000000001000;13-16 000000000000000000000001000;14-15 000000000000000000000001000;14-16 000000000000000000000001000;15-16 000000000000000000000001000;15-17  00000000000000000000001000;15-18                         ;16-17  00000000000000000000001000;16-18                          ;17-18  
		String path ="10000000000000000000000000000000;10001000000000000000000000000000;10001001000000000000000000000000;00100000000000000000000000000000;00100000000010000000000000000000;00100000000010010000000000000000;00100000000010010100000000000000;00100000000001000000000000000000;00100000000001000000100000000000;00100000000001000000100100000000;00100000000001000000010000000000;00100000000001000000001000000000;10000010000000000000000000000000;10001000010000000000000000000000;10001001000100000000000000000000;00001000000000000000000000000000;00001001000000000000000000000000;00000100000010000000000000000000;00000100000000000000000000000000;00001000100000000000000000000000;00001000100000000100000000000000;00000100000000001000100000000000;00000100000000001000000000000000;00001000100000000010000000000000;00001000100000000010000001000000;00000100000000001000101000000000;00000010000000000000000000000000;00001000010000000000000000000000;00001001000100000000000000000000;00000001000000000000000000000000;00000000100010010000000000000000;00000000100000010000000000000000;00000000100000000000000000000000;00000000100000000100000000000000;00000000100000000010100100000000;00001100000000001000000000000000;00000000100000000010000000000000;00000000100000000010000001000000;00000000100011010000001000000000;00001010000000000000000000000000;00000000010000000000000000000000;00000000100000000010000001010000;00001101000010000000000000000000;00000001100000010000000000000000;00000001100000000000000000000000;00000001100000000100000000000000;00001101000011000000000000000000;00001101000000001000000000000000;00000001100000000010000000000000;00000001100000000010000001000000;00001101000011000000001000000000;00001011000000000000000000000000;00000001010000000000000000000000;00000000000100000000000000000000;00000000000010000000000000000000;00000000000010010000000000000000;00000000000000100000000000000000;00000000000001000000000000000000;00000000000010001000000000000000;00000000000010010010000000000000;00000000000010010010000001000000;00000000000001000000001000000000;00000000000001000000100010000000;00000000000010010010000000100000;00000000000001000000001000000100;00000000000000010000000000000000;00000000000000010100000000000000;00000000000011000000000000000000;00000000000000001000000000000000;00000000000000010010000000000000;00000000000000010010000001000000;00000000000011000000001000000000;00000000000000001000000010000000;00000000000000010010000000100000;00001101000100000000000000000000;00000000000000000100000000000000;00000000000000000101010000000000;00000000000000011000000000000000;00000000000000000010000000000000;00000000000011010000010000000000;00000000000011010000001000000000;00000000000000011000000010000000;00000000000000000010000000100000;00000001100100000000000000000000;00000000000000000110100100000000;00000000000000011100000000000000;00000000000000000110000000000000;00000000000000000110000001000000;00000000000001100000001000000000;00001010100000000100000000000000;00000000000000000110000000100000;00000000000000000110000000100001;00000000000000000000100000000000;00000000000000000000100100000000;00000000000000000000100101000000;00000000000000000000001000000000;00000000000000000000100010000000;00000000000000000000100100100000;00000000000000000000100100100001;00000000000000000000000100000000;00000000000000000000000101000000;00000000000000000000101000000000;00000000000000000000000010000000;00000000000000000000000100100000;00000001100100000010000100000000;00000000000000000000000001000000;00000000000000000000101100000000;00000000000000000000000110000000;00000000000000000000000000100000;00000001100100000010000000000000;00000000000000000000101101000000;00000000000000000000000111000000;00000000000000000000000001100000;00000000001100000001000000000000;00000000000000000000101010000000;00000000000000000000101100100000;00000000000000000000101100100001;00000000000000000000000110100000;00000000000000000000000000001100;00000000000000000000000000001110";//getParameter("work");00000 00000 00000 00000 0000
		StringTokenizer t1 = new StringTokenizer(path, ";");
                            //   1-200101000000000000000000000000100     ;1-310000000000000000000;1-4000000000000000000000001000;1-50010100000000000000000000000000;1-600101000000000000000000000000;1-700101000000000000000000000001000;1-800101000000000000000000000;1-900101000000000000000000000000;1-100010100000000000000000000000;1-110001010000000000000000001000; 1-12 10100000000000000000000000;1-13 01000000000000000000000001000;1-14010000000000000000000000010;1-1501000000000000000000000001000;1-1600100000000000000000000000;2-3101000000000000000000000001000;2-41010000000000000000000000000;2-5000000000000000000000001000;2-611010000000000000000000001000;2-710100000000000000000000001000;2-801000000000000000000000001000;2-941010000000000000000000001000;2-10 1000000000000000000000001000;2-11 01000000000000000000001000;2-12 000000000000000000000001000;2-13 000000000000000000000001000;2-14 000000000000000000000001000;2-15 000000000000000000000001000;2-16 000000000000000000000001000; 3 -4101000000000000000000000001000;3-5000000000000000000000001000;3-611010000000000000000000001000;3-710100000000000000000000001000;3-801000000000000000000000001000;3-941010000000000000000000001000;3-10 1000000000000000000000001000;3-11 01000000000000000000001000;3-12 000000000000000000000001000;3-13 000000000000000000000001000;3-14 000000000000000000000001000;3-15 000000000000000000000001000;3-16 000000000000000000000001000;4-500000000000000000000001000   ;4-611010000000000000000000001000;4-710100000000000000000000001000;4-801000000000000000000000001000;4-941010000000000000000000001000;4-10 1000000000000000000000001000;4-11 01000000000000000000001000;4-12 000000000000000000000001000;4-13 000000000000000000000001000;4-14 000000000000000000000001000;4-15 000000000000000000000001000;4-16 000000000000000000000001000 ;5-611010000000000000000000001000;5-710100000000000000000000001000;5-801000000000000000000000001000;5-941010000000000000000000001000;5-10 1000000000000000000000001000;5-11 01000000000000000000001000;5-12 000000000000000000000001000;5-13 000000000000000000000001000;5-14 000000000000000000000001000;5-15 000000000000000000000001000;5-16 000000000000000000000001000;6-710100000000000000000000001000;6-801000000000000000000000001000;6-941010000000000000000000001000;6-10 1000000000000000000000001000;6-11 01000000000000000000001000;6-12 000000000000000000000001000;6-13 000000000000000000000001000;6-14 000000000000000000000001000;6-15 000000000000000000000001000;6-16 000000000000000000000001000;7-801000000000000000000000001000;7-941010000000000000000000001000;7-10 1000000000000000000000001000;7-11 01000000000000000000001000;7-12 000000000000000000000001000;7-13 000000000000000000000001000;7-14 000000000000000000000001000;7-15 000000000000000000000001000;7-16 000000000000000000000001000;8-941010000000000000000000001000;8-10 1000000000000000000000001000;8-11 01000000000000000000001000;8-12 000000000000000000000001000;8-13 000000000000000000000001000;8-14 000000000000000000000001000;8-15 000000000000000000000001000;8-16 000000000000000000000001000;9-10 1000000000000000000000001000;9-11 01000000000000000000001000;9-12 000000000000000000000001000;9-13 000000000000000000000001000;9-14 000000000000000000000001000;9-15 000000000000000000000001000;9-16 0000000000000000000000010 ;10-11 010000000000000000000010;10-12 000000000000000000000001000;10-13 0000000000000000000001000;10-14 0000000000000000000001000;10-15 000000000000000000000001000;10-16 000000000000000000000001000;11-12 000000000000000000000001000;11-13 0000000000000000000001000;11-14 0000000000000000000001000;11-15 000000000000000000000001000;11-16 000000000000000000000001000;12-13 0000000000000000000001000;12-14 0000000000000000000001000;12-15 000000000000000000000001000;12-16 000000000000000000000001000;13-14 0000000000000000000001000;13-15 000000000000000000000001000;13-16 000000000000000000000001000;14-15 000000000000000000000001000;14-16 000000000000000000000001000;15-16 000000000000000000000001000;15-17  00000000000000000000001000;15-18                         ;16-17  00000000000000000000001000;16-18                          ;17-18  
		String path2 = "01011011000000000000000000001000;01100001100010010000000000000000;01100000001000100000000000000000;10010100000011000000001000000000;11000100001000010100000000000000;11001000101000000100000000000000;01010000001000000001000000010100;10010100000000001000101000000000;10010100000000001000000010001000;10010100000000010010000000101010;01010000001000000001000000010100;10010010000000000000000000001000;01010000000100000000000000001011;01010000000100000000000000001011;01010000001000000001000000010100;11000101100000010000000000000000;11000100001000010100000000000000;10101000100001000010100100000000;10101000100010010000000000000000;11000100001000010100000000000000;01100000001000100000000000000000;10101001001001000001010000000000;00001010100000000010000110000000;11000100001000001001000101000000;11001010001000000001000000010011;10011001000100000000000000000100;10010100000000001000000010001000;11000010000100000000000000000011;11000010001000000001000000010011;11001000101000000100000000000000;10101001001010100000000000000000;00001101001000001001000101000000;00001101001000010100000000000000;00001101001000001001000101000000;10101001000101000000001000000100;01100001100001000010100100000000;00000001011000000001000001100000;00001011001000000001000000010011;10011001000100000000000000000100;01010001010000000000000000001010;00001010100000000010000000100010;00000001010100000000000000000001;01100000001001000001010000000000;01100000001010001001000101000000;01100000001010010100000000000000;01100000001001000001010000000000;01010000001000000001011000000000;01010000001000000001101101000000;01100000001001000001100101000000;01010000001000000001000000010100;01010000001000000001000000011011;01010000001000000001000000011011;01010000001000000001000000011011;01010000001000000001000000010100;10100100000001001000100000000000;10101000100001000010100100000000;01100000001010010100000000000000;00110000000010001000101000000000;10101000100001000010100100000000;10101001001001000001100101000000;00000000000001100001010000000000;00110000000010001000000010001000;00111100010010000000000000001010;00000000000001100001001000011011;00110000000000100001000000011011;00001100100000001010000100000000;00001101001000001001000101000000;10010100000000001000101000000000;00000000000011010010100100000000;00000000000010101001000101000000;00001101001000001001000010010011;10010100000000001000000010001000;00000110000000010010000000100010;00001100010000001000000010000010;00000000000000011101000010010011;00000001101000000011000001000000;00000000000011010010110100000000;00001010100000000010000110000000;00000000000000011101000101000000;10011000100000000101000000010100;10011000100000000101000000010100;00001010100000000010000000100010;00000000110000000111000000110001;00000000000000000111000000110001;00000000000001100001010000000000;00000000000001100001100101000000;00000000000010101001000101000000;00000000000001100001010000000000;01010000001000000001000000010100;01010000001000000001000000011011;00000001011000000001000000010001;00000000001100000001000000010000;00000000000000000000011111001000;00000000000000000000011001101010;00000000000000000000011000010100;00110000000001000000010000011000;00000000000000000000011000011011;00000000000000000000011000011011;00000000000000000000011000010100;00000000000000000000110011100010;00000000000000000000110010010011;10010100000000001000000010001000;00000000000000000000101100101010;00000000000000011101000010010011;11000100001000001001000010010011;00000000000000000111000000110001;10011000100000000010000000101010;00000000000000000000011001101010;00000000000000000000000111010011;00000000000000000000000001110001;01010000001000000001000000010100;00000000000000000000011000011011;00000000000000000000011000011011;00000000000000000000000001110001;00000000000000000000000000001111;00110000000001000000010000011011;01010000000100000000000000000100;00000000000000000000000000001111;11000010000100000000000000000011;00000000000000000000000001110001";//getParameter("backup");//"00000 00000 00000 00000 00000 00000 00//
		StringTokenizer t2 = new StringTokenizer(path2,";");
                
                
		String str1,str2;
		for (int i=0; i <panel.nnodes-1; i++){
			for (int j=i+1; j <panel.nnodes; j++)
                        {
				str1 = t1.nextToken();
                                //System.out.println(str1);
				str2 = t2.nextToken();
                                //System.out.print(str2);
                                //System.out.print("/n");
                                //System.out.print(i);System.out.print(j);
                                //System.out.print("\n");
				panel.addFlow(i, j, str1, str2);
			}
		}
    
	}
       catch(Exception e){}  
}
   
    public void destroy() {
        remove(panel);
        remove(controlPanel);
    }

    public void start() {
	panel.start();
    }

    public void stop() {
	panel.stop();
    }

    public void actionPerformed(ActionEvent e) {
	Object src = e.getSource();

	if (src == state) {
		StringBuffer x = new StringBuffer(); 
		double minx = 400, miny=400,maxx=0,maxy=0;
	    for (int i = 0 ; i < panel.nnodes ; i++) {
			panel.nodes[i].fixed = true;
			minx = Math.min(minx,panel.nodes[i].x);
			maxx = Math.max(maxx,panel.nodes[i].x);
			miny = Math.min(miny,panel.nodes[i].y);
			maxy = Math.max(maxy,panel.nodes[i].y);
			x= x.append("\\rput(")
				.append(Math.floor(panel.nodes[i].x)).append(",")
				  .append(-Math.floor(panel.nodes[i].y)).append("){\\rnode{n")
				  .append(i+1).append("}{\\pscirclebox{")
				.append(i+1).append("}}} ");
		}
		for (int i = 0; i < panel.nedges ; i++) {
			x = x.append("\\ncline{n").append(panel.edges[i].from+1)
				 .append("}{n").append(panel.edges[i].to+1)
				 .append("} ");
		}
		String c = "\\pspicture*("+Math.floor(minx-15)
				   +","+Math.floor(-miny+15)
				   +")(" + Math.floor(maxx+15) 
				   + ","+ Math.floor(-maxy-15) +") "
			+x.toString();
//		loc.setText(c);
		topo = !topo;
//		loc.setEnabled(topo);
		scramble.setEnabled(!topo);
		shake.setEnabled(!topo);
		stress.setEnabled(!topo);
		random.setEnabled(!topo);
		
		if (topo)
			state.setLabel("Show Paths");
                
                
		else 
			state.setLabel("Topology");
                
		
		return;
	}

	if (src == scramble) {
	  //  play(getCodeBase(), "audio/computer.au");
	    Dimension d = getSize();
	    for (int i = 0 ; i < panel.nnodes ; i++) {
		Node2 n = panel.nodes[i];
		if (!n.fixed) {
		    n.x = 10 + (d.width-20)*Math.random();
		    n.y = 10 + (d.height-20)*Math.random();
		}
	    }
	    return;
	}
	if (src == shake)
        {
	    //play(getCodeBase(), "audio/gong.au");
	    Dimension d = getSize();
	    for (int i = 0 ; i < panel.nnodes ; i++)
            {
		Node2 n = panel.nodes[i];
		if (!n.fixed)
                {
		    n.x += 80*Math.random() - 40;
		    n.y += 80*Math.random() - 40;
		}
	    }
	}
     /*   if(src==next)
                {
                 String savedstis=t1.getText();
                 String savedsbpl=t2.getText();
                 System.out.print(savedsbpl);
                }*/
        

    }

    public void itemStateChanged(ItemEvent e) {
	Object src = e.getSource();
	boolean on = e.getStateChange() == ItemEvent.SELECTED;
	if (src == stress) panel.stress = on;
	else if (src == random) panel.random = on;
    }

    public String getAppletInfo() {
	return "Title: GraphLayout \nAuthor: Yu Liu <yuliu@tele.pitt.edu>";
    }

    public String[][] getParameterInfo() {
	String[][] info = {
	    {"edges", "delimited string", "A comma-delimited list of all the edges.  It takes the form of 'C-N1,C-N2,C-N3,C-NX,N1-N2/M12,N2-N3/M23,N3-NX/M3X,...' where C is the name of center node (see 'center' parameter) and NX is a node attached to the center node.  For the edges connecting nodes to eachother (and not to the center node) you may (optionally) specify a length MXY separated from the edge name by a forward slash."},
	    {"center", "string", "The name of the center node."},
		{"fixed", "string:x:y,...,string:x:y", "The names and locations of the fixed nodes."}
	};
	return info;
    }

}

