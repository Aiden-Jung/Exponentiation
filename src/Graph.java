/** Aiden Hyunseok Jung & Hwi Jin Jang
 *  Department of Computer Science
 *  Grinnell College
 *  junghyun@grinnell.edu & janghwij@grinnell.edu
 *
 *  An object of the Graph class draws a graph from the given data and find the shortest
 *  paths in various ways.
 *  
 *  This is the given Graph class. We modify the printPath, processRequest,
 *  generateFileForGraph, and main method for the assignment.
 */

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;

import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.Map;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Scanner;


// Used to signal violations of preconditions for
// various shortest path algorithms.
class GraphException extends RuntimeException
{
    public GraphException( String name )
    {
        super( name );
    }
}

// Represents an edge in the graph.
class Edge
{
    public Vertex     dest;   // Second vertex in Edge
    public double     cost;   // Edge cost
    
    public Edge( Vertex d, double c )
    {
        dest = d;
        cost = c;
    }
}

// Represents an entry in the priority queue for Dijkstra's algorithm.
class Path implements Comparable<Path>
{
    public Vertex     dest;   // w
    public double     cost;   // d(w)
    
    public Path( Vertex d, double c )
    {
        dest = d;
        cost = c;
    }
    
    public int compareTo( Path rhs )
    {
        double otherCost = rhs.cost;
        
        return cost < otherCost ? -1 : cost > otherCost ? 1 : 0;
    }
}

// Represents a vertex in the graph.
class Vertex
{
    public String     name;   // Vertex name
    public List<Edge> adj;    // Adjacent vertices
    public double     dist;   // Cost
    public Vertex     prev;   // Previous vertex on shortest path
    public int        scratch;// Extra variable used in algorithm

    public Vertex( String nm )
      { name = nm; adj = new LinkedList<Edge>( ); reset( ); }

    public void reset( )
      { dist = Graph.INFINITY; prev = null; pos = null; scratch = 0; }    
      
    public PairingHeap.Position<Path> pos;  // Used for dijkstra2 (Chapter 23)
}

// Graph class: evaluate shortest paths.
//
// CONSTRUCTION: with no parameters.
//
// ******************PUBLIC OPERATIONS**********************
// void addEdge( String v, String w, double cvw )
//                              --> Add additional edge
// void printPath( String w )   --> Print path after alg is run
// void unweighted( String s )  --> Single-source unweighted
// void dijkstra( String s )    --> Single-source weighted
// void negative( String s )    --> Single-source negative weighted
// void acyclic( String s )     --> Single-source acyclic
// ******************ERRORS*********************************
// Some error checking is performed to make sure graph is ok,
// and to make sure graph satisfies properties needed by each
// algorithm.  Exceptions are thrown if errors are detected.

public class Graph
{
    public static final double INFINITY = Double.MAX_VALUE;
    private Map<String,Vertex> vertexMap = new HashMap<String,Vertex>( );

    /**
     * Add a new edge to the graph.
     */
    public void addEdge( String sourceName, String destName, double cost )
    {
        Vertex v = getVertex( sourceName );
        Vertex w = getVertex( destName );
        v.adj.add( new Edge( w, cost ) );
    }

    /**
     * Driver routine to handle unreachables and print total cost.
     * It calls recursive routine to print shortest path to
     * destNode after a shortest path algorithm has run.
     */
    
    /* We modify the printPath method to pass a PrintWriter and to print
     * the output in a particular file.
     * The printPath method takes a String, which is the name of the given
     * Vertex, and a PrintWriter as arguments and prints the path into a particular file.
     * 
     * @param destName a name of the given Vertex
     * 	      pw       a PrintWriter
     */
	public void printPath(String destName, PrintWriter pw) {
		Vertex w = vertexMap.get(destName);
		if (w == null)
			throw new NoSuchElementException("Destination vertex not found");
		else if (w.dist == INFINITY)
			pw.println(destName + " is unreachable");
		else {
			pw.print("(Cost is: " + w.dist + ") ");
			printPath(w, pw);
			pw.println();
		}
	}

    /**
     * If vertexName is not present, add it to vertexMap.
     * In either case, return the Vertex.
     */
    private Vertex getVertex( String vertexName )
    {
        Vertex v = vertexMap.get( vertexName );
        if( v == null )
        {
            v = new Vertex( vertexName );
            vertexMap.put( vertexName, v );
        }
        return v;
    }

    /**
     * Recursive routine to print shortest path to dest
     * after running shortest path algorithm. The path
     * is known to exist.
     */
    /* This printPath method is the internal method.
     * We modify the internal printPath method to pass a PrintWriter and to print
     * the output in a particular file.
     * The internal printPath method takes a String, which is the name of the given
     * Vertex, and a PrintWriter as arguments and prints the path into a particular file.
     * 
     * @param destName a name of the given Vertex
     * 	      pw       a PrintWriter
     */
	private void printPath(Vertex dest, PrintWriter pw) {
		if (dest.prev != null) {
			printPath(dest.prev, pw);
			pw.print(" to ");
		}
		pw.print(dest.name);
	}

    
    /**
     * Initializes the vertex output info prior to running
     * any shortest path algorithm.
     */
    private void clearAll( )
    {
        for( Vertex v : vertexMap.values( ) )
            v.reset( );
    }

    /**
     * Single-source unweighted shortest-path algorithm.
     */
    public void unweighted( String startName )
    {
        clearAll( ); 

        Vertex start = vertexMap.get( startName );
        if( start == null )
            throw new NoSuchElementException( "Start vertex not found" );

        Queue<Vertex> q = new LinkedList<Vertex>( );
        q.add( start ); start.dist = 0;

        while( !q.isEmpty( ) )
        {
            Vertex v = q.remove( );

            for( Edge e : v.adj )
            {
                Vertex w = e.dest;
                if( w.dist == INFINITY )
                {
                    w.dist = v.dist + 1;
                    w.prev = v;
                    q.add( w );
                }
            }
        }
    }

    /**
     * Single-source weighted shortest-path algorithm.
     */
    public void dijkstra( String startName )
    {
        PriorityQueue<Path> pq = new PriorityQueue<Path>( );

        Vertex start = vertexMap.get( startName );
        if( start == null )
            throw new NoSuchElementException( "Start vertex not found" );

        clearAll( );
        pq.add( new Path( start, 0 ) ); start.dist = 0;
        
        int nodesSeen = 0;
        while( !pq.isEmpty( ) && nodesSeen < vertexMap.size( ) )
        {
            Path vrec = pq.remove( );
            Vertex v = vrec.dest;
            if( v.scratch != 0 )  // already processed v
                continue;
                
            v.scratch = 1;
            nodesSeen++;

            for( Edge e : v.adj )
            {
                Vertex w = e.dest;
                double cvw = e.cost;
                
                if( cvw < 0 )
                    throw new GraphException( "Graph has negative edges" );
                    
                if( w.dist > v.dist + cvw )
                {
                    w.dist = v.dist +cvw;
                    w.prev = v;
                    pq.add( new Path( w, w.dist ) );
                }
            }
        }
    }

    /**
     * Single-source weighted shortest-path algorithm using pairing heaps.
     */
    public void dijkstra2( String startName )
    {
        PairingHeap<Path> pq = new PairingHeap<Path>( );

        Vertex start = vertexMap.get( startName );
        if( start == null )
            throw new NoSuchElementException( "Start vertex not found" );

        clearAll( );
        start.pos = pq.insert( new Path( start, 0 ) ); start.dist = 0;

        while ( !pq.isEmpty( ) )
        {
            Path vrec = pq.deleteMin( );
            Vertex v = vrec.dest;
                
            for( Edge e : v.adj )
            {
                Vertex w = e.dest;
                double cvw = e.cost;
                
                if( cvw < 0 )
                    throw new GraphException( "Graph has negative edges" );
                    
                if( w.dist > v.dist + cvw )
                {
                    w.dist = v.dist + cvw;
                    w.prev = v;
                    
                    Path newVal = new Path( w, w.dist );                    
                    if( w.pos == null )
                        w.pos = pq.insert( newVal );
                    else
                        pq.decreaseKey( w.pos, newVal ); 
                }
            }
        }
    }

    /**
     * Single-source negative-weighted shortest-path algorithm.
     */
    public void negative( String startName )
    {
        clearAll( ); 

        Vertex start = vertexMap.get( startName );
        if( start == null )
            throw new NoSuchElementException( "Start vertex not found" );

        Queue<Vertex> q = new LinkedList<Vertex>( );
        q.add( start ); start.dist = 0; start.scratch++;

        while( !q.isEmpty( ) )
        {
            Vertex v = q.remove( );
            if( v.scratch++ > 2 * vertexMap.size( ) )
                throw new GraphException( "Negative cycle detected" );

            for( Edge e : v.adj )
            {
                Vertex w = e.dest;
                double cvw = e.cost;
                
                if( w.dist > v.dist + cvw )
                {
                    w.dist = v.dist + cvw;
                    w.prev = v;
                      // Enqueue only if not already on the queue
                    if( w.scratch++ % 2 == 0 )
                        q.add( w );
                    else
                        w.scratch--;  // undo the enqueue increment    
                }
            }
        }
    }

    /**
     * Single-source negative-weighted acyclic-graph shortest-path algorithm.
     */
    public void acyclic( String startName )
    {
        Vertex start = vertexMap.get( startName );
        if( start == null )
            throw new NoSuchElementException( "Start vertex not found" );

        clearAll( ); 
        Queue<Vertex> q = new LinkedList<Vertex>( );
        start.dist = 0;
        
          // Compute the indegrees
		Collection<Vertex> vertexSet = vertexMap.values( );
        for( Vertex v : vertexSet )
            for( Edge e : v.adj )
                e.dest.scratch++;
            
          // Enqueue vertices of indegree zero
        for( Vertex v : vertexSet )
            if( v.scratch == 0 )
                q.add( v );
       
        int iterations;
        for( iterations = 0; !q.isEmpty( ); iterations++ )
        {
            Vertex v = q.remove( );

            for( Edge e : v.adj )
            {
                Vertex w = e.dest;
                double cvw = e.cost;
                
                if( --w.scratch == 0 )
                    q.add( w );
                
                if( v.dist == INFINITY )
                    continue;    
                
                if( w.dist > v.dist + cvw )
                {
                    w.dist = v.dist + cvw;
                    w.prev = v;
                }
            }
        }
        
        if( iterations != vertexMap.size( ) )
            throw new GraphException( "Graph has a cycle!" );
    }

    /**
     * Process a request; return false if end of file.
     */
    /* We modify the processRequest method to pass a File and store the output in the File.
     * The processRequest method takes a Graph and a File as arguments and
     * returns true unless the NoSuchElementException is caught.
     * 
     * @param g a given Graph
     * 	      f a File where the output is stored
     * 
     * @return a boolean value depending on whether the NoSuchElementException
     *         is caught
     */
	public static boolean processRequest(Graph g, File f) {

		PrintWriter shortestPathFile = null;

		try {
			FileWriter fw = new FileWriter(f);
			shortestPathFile = new PrintWriter(fw);

			try {
				//Print the shortest paths from 0 to every other vertex using the dijkstra algorithm
				String startName = Integer.toString(0);
				g.dijkstra(startName);
				
				for (int i = 1; i <= 1000; i++) {
					shortestPathFile.print("Start: " + startName + ", ");

					String destName = Integer.toString(i);
					shortestPathFile.print("End: " + destName + " ");

					g.printPath(destName, shortestPathFile);
				}
				
				
			} catch (NoSuchElementException e) {
				return false;
			} catch (GraphException e) {
				System.err.println(e);
			}

		} catch (IOException e) {
			System.err.println(e);
		} finally {
			if (shortestPathFile != null) {
				shortestPathFile.close();
			}
		}
		return true;
	}

    /**
     * a method that constructs a file. This
     * file contains data about vertices and edge costs. 
     */
    /* We modify the generateFileForGraph method to construct an input file for a graph.
     * The generateFileForGraph method takes a String, which is the file path, and
     * constructs an input file for a graph
     * 
     * @param filePath the given file path
     */
	public void generateFileForGraph(String filePath) {
		String outputFileName = filePath;
		PrintWriter graphFileExponent = null;

		try {
			FileWriter fout = new FileWriter(outputFileName);
			graphFileExponent = new PrintWriter(fout);

			String line = "";
			for (int exponent = 0; exponent < 1000; exponent++) {
				/*
				 * write data about vertices (exponents) and edges when an edge connects
				 * an exponent to the next greater exponent. You may use the 'line' variable for
				 * storing the data.
				 */
				
				/* According to the documentation, the method needs to write double-type
				 * edge weights to the input file, so we converts edge weights to double
				 * just in case although the value doesn't change.
				 */
				line = exponent + " " + (exponent + 1) + " " + (double)exponent;
				StringTokenizer st = new StringTokenizer(line);

				try {
					if (st.countTokens() != 3) {
						System.err.println("Skipping ill-formatted line " + line);
						continue;
					}
					String source = st.nextToken();
					String dest = st.nextToken();
					String cost = st.nextToken();

					graphFileExponent.println(source + " " + dest + " " + cost);

					/*
					 * write data about vertices (exponents) and edges when an edge connects
					 * an exponent to its doubled value. You may use 'line' variable for storing the
					 * data.
					 */
					
					/* As mentioned in the documentation, the definition of calculating the edge weights for
					 * edges connecting from exponents to their doubled values is not necessary for 0, 1, and
					 * the exponents whose doubled values are greater than 1000.
					 */
					if (exponent != 0 && exponent != 1 && exponent * 2 <= 1000) {
						line = exponent + " " + (exponent * 2) + " " + exponent * (1 + Math.log(exponent) / Math.log(2));
						st = new StringTokenizer(line);

						if (st.countTokens() != 3) {
							System.err.println("Skipping ill-formatted line " + line);
							continue;
						}
						source = st.nextToken();
						dest = st.nextToken();
						cost = st.nextToken();

						graphFileExponent.println(source + " " + dest + " " + cost);
					}

				} catch (NumberFormatException e) {
					System.err.println("Skipping ill-formatted line " + line);
				}
			}
		} catch (IOException e) {
			System.err.println(e);
		} finally {
			if (graphFileExponent != null) {
				graphFileExponent.close();
			}
		}
	}
    /**
     * A main routine that:
     * 1. Reads a file containing edges (supplied as a command-line parameter);
     * 2. Forms the graph;
     * 3. Repeatedly prompts for two vertices and
     *    runs the shortest path algorithm.
     * The data file is a sequence of lines of the format
     *    source destination cost
     */
	//We modify the main method for the assignment.
	public static void main(String[] args) {
		Graph g = new Graph();
		/*
		 * call the generateFileForGraph method here.
		 */
		g.generateFileForGraph(args[0]);
		System.out.println("The input file is created!");
		try {
			FileReader fin = new FileReader(args[0]);
			Scanner graphFile = new Scanner(fin);

			// Read the edges and insert
			String line;
			while (graphFile.hasNextLine()) {
				line = graphFile.nextLine();
				StringTokenizer st = new StringTokenizer(line);

				try {
					if (st.countTokens() != 3) {
						System.err.println("Skipping ill-formatted line " + line);
						continue;
					}
					String source = st.nextToken();
					String dest = st.nextToken();
					double cost = Double.parseDouble(st.nextToken());
					g.addEdge(source, dest, cost);
				} catch (NumberFormatException e) {
					System.err.println("Skipping ill-formatted line " + line);
				}
			}
		} catch (IOException e) {
			System.err.println(e);
		}

		System.out.println("File read...");
		System.out.println(g.vertexMap.size() + " vertices");

		/*
		 * declare a file for printing the shortest paths. Pass the file, as
		 * another argument, to processRequest to display all the shortest paths. Each
		 * shortest path is displayed after returning from the Dijkstra's algorithm.
		 * 
		 * Note: You do not need System.in to read the names of vertices from the
		 * command prompt.
		 */
		File f = new File(args[1]);
		processRequest(g, f);
		System.out.println("The output file is created!");
	}
}