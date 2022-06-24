package it.polito.tdp.imdb.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.imdb.db.ImdbDAO;

public class Model {
	
	private ImdbDAO dao;
	private Graph<Director, DefaultWeightedEdge> graph;
	private Map<Integer, Director> idMap;
	private List<Director> best;
	private int totAttori;
	
	public Model() {
		
		this.dao = new ImdbDAO();
		this.idMap = new HashMap<Integer, Director>();
		this.dao.listAllDirectors(this.idMap);
	}
	
	public void creaGrafo(Integer year) {
		
		this.graph = new SimpleWeightedGraph<Director, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		List<Director> directors = this.dao.directors(year);
		
		Graphs.addAllVertices(this.graph, directors);
		
		List<Arco> edges = this.dao.getArchi(year, this.idMap);
		
		for(Arco a : edges) {
			if(!this.graph.containsEdge(a.getD1(), a.getD2()) && !this.graph.containsEdge(a.getD2(), a.getD1())) {
				Graphs.addEdge(this.graph, a.getD1(), a.getD2(), a.getPeso());
			}
		}
		
		
		System.out.println("Grafo creato!"+"\n");
		System.out.println("#Vertici: "+this.graph.vertexSet().size()+"\n");
		System.out.println("#Archi: "+this.graph.edgeSet().size()+"\n");
	}
	
	public List<Director> directors(int year){
		return this.dao.directors(year);
	}
	
	public int nVertices() {
		return this.graph.vertexSet().size();
	}
	
	public int nEdges() {
		return this.graph.edgeSet().size();
	}
	
	public List<Director> adiacenti(Director partenza){
		return Graphs.neighborListOf(this.graph, partenza);
	}
	
	public String listaAdiacenti(Director partenza) {
		
		String result = "";
		List<Director> adiacenti = this.adiacenti(partenza);
		
		for(Director d : adiacenti) {
			result += d+" - Attori condivisi: "+(int) this.graph.getEdgeWeight(this.graph.getEdge(partenza, d))+"\n";
		}
		
		return result;
	}
	
	public String cerca(Director partenza, int c) {
		int tot = 0;
		String result = "";
		this.best = new ArrayList<>();
		this.totAttori = 0;
		List<Director> parziale = new ArrayList<>();
		parziale.add(partenza);
		ricorsione(parziale, partenza, c, tot);
		for(Director d : this.best) {
			result += d+"\n";
		}
		
		result += "Totale attori condivisi: "+this.totAttori;
		
		return result;
	}

	private void ricorsione(List<Director> parziale, Director partenza, int c, int tot) {

		//CONDIZIONE DI TERMINAZIONE
		if(parziale.size()>this.best.size()) {
			if(tot<=c) {
				this.best = new ArrayList<Director>(parziale);
				this.totAttori=tot;
			}
			else {
				return;
			}
		}
	
		
		for(Director d : Graphs.neighborListOf(this.graph, partenza)) {
			if((tot+this.graph.getEdgeWeight(this.graph.getEdge(partenza, d)))<=c && !parziale.contains(d)){
				
				parziale.add(d);
				tot+=this.graph.getEdgeWeight(this.graph.getEdge(partenza, d));
				ricorsione(parziale,d,c,tot);
				parziale.remove(parziale.size()-1);
				tot=tot-(int)this.graph.getEdgeWeight(this.graph.getEdge(partenza, d));
		
			}
		}
	}

}
