/*******************************************************************************************************
 *
 * gama.kernel.batch.GeneticAlgorithm.java, in plugin gama.core, is part of the source code of the GAMA modeling
 * and simulation platform (v. 1.8)
 *
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.kernel.batch;

import java.util.List;
import java.util.stream.Collectors;

import gama.common.interfaces.ICollector;
import gama.common.interfaces.IKeyword;
import gama.common.interfaces.batch.CrossOver;
import gama.common.interfaces.batch.Initialization;
import gama.common.interfaces.batch.Mutation;
import gama.common.interfaces.batch.Selection;
import gama.common.interfaces.experiment.IExperimentPlan;
import gama.common.interfaces.experiment.IParameter;
import gama.common.util.Collector;
import gama.kernel.experiment.BatchAgent;
import gama.kernel.experiment.ParameterAdapter;
import gama.kernel.experiment.ParametersSet;
import gama.processor.annotations.IConcept;
import gama.processor.annotations.ISymbolKind;
import gama.processor.annotations.GamlAnnotations.doc;
import gama.processor.annotations.GamlAnnotations.example;
import gama.processor.annotations.GamlAnnotations.facet;
import gama.processor.annotations.GamlAnnotations.facets;
import gama.processor.annotations.GamlAnnotations.inside;
import gama.processor.annotations.GamlAnnotations.symbol;
import gama.processor.annotations.GamlAnnotations.usage;
import gama.runtime.exceptions.GamaRuntimeException;
import gama.runtime.scope.IScope;
import gaml.descriptions.IDescription;
import gaml.expressions.IExpression;
import gaml.operators.Cast;
import gaml.types.IType;

@symbol (
		name = IKeyword.GENETIC,
		kind = ISymbolKind.BATCH_METHOD,
		with_sequence = false,
		concept = { IConcept.BATCH, IConcept.ALGORITHM })
@inside (
		kinds = { ISymbolKind.EXPERIMENT })
@facets (
		value = { @facet (
				name = IKeyword.NAME,
				type = IType.ID,
				optional = false,
				internal = true,
				doc = @doc ("The name of this method. For internal use only")),
				@facet (
						name = GeneticAlgorithm.POP_DIM,
						type = IType.INT,
						optional = true,
						doc = @doc ("size of the population (number of individual solutions)")),
				@facet (
						name = GeneticAlgorithm.CROSSOVER_PROB,
						type = IType.FLOAT,
						optional = true,
						doc = @doc ("crossover probability between two individual solutions")),
				@facet (
						name = GeneticAlgorithm.MUTATION_PROB,
						type = IType.FLOAT,
						optional = true,
						doc = @doc ("mutation probability for an individual solution")),
				@facet (
						name = GeneticAlgorithm.NB_GEN,
						type = IType.INT,
						optional = true,
						doc = @doc ("number of random populations used to build the initial population")),
				@facet (
						name = GeneticAlgorithm.MAX_GEN,
						type = IType.INT,
						optional = true,
						doc = @doc ("number of generations")),
				@facet (
						name = GeneticAlgorithm.IMPROVE_SOL,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("if true, use a hill climbing algorithm to improve the solutions at each generation")),
				@facet (
						name = GeneticAlgorithm.STOCHASTIC_SEL,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("if true, use a stochastic selection algorithm (roulette) rather a determistic one (keep the best solutions)")),
				@facet (
						name = IKeyword.MAXIMIZE,
						type = IType.FLOAT,
						optional = true,
						doc = @doc ("the value the algorithm tries to maximize")),
				@facet (
						name = IKeyword.MINIMIZE,
						type = IType.FLOAT,
						optional = true,
						doc = @doc ("the value the algorithm tries to minimize")),
				@facet (
						name = IKeyword.AGGREGATION,
						type = IType.LABEL,
						optional = true,
						values = { IKeyword.MIN, IKeyword.MAX },
						doc = @doc ("the agregation method")) },
		omissible = IKeyword.NAME)
@doc (
		value = "This is a simple implementation of Genetic Algorithms (GA). See the wikipedia article and [batch161 the batch dedicated page]. The principle of the GA is to search an optimal solution by applying evolution operators on an initial population of solutions. There are three types of evolution operators: crossover, mutation and selection. Different techniques can be applied for this selection. Most of them are based on the solution quality (fitness).",
		usages = { @usage (
				value = "As other batch methods, the basic syntax of the `genetic` statement uses `method genetic` instead of the expected `genetic name: id` : ",
				examples = { @example (
						value = "method genetic [facet: value];",
						isExecutable = false) }),
				@usage (
						value = "For example: ",
						examples = { @example (
								value = "method genetic maximize: food_gathered pop_dim: 5 crossover_prob: 0.7 mutation_prob: 0.1 nb_prelim_gen: 1 max_gen: 20; ",
								isExecutable = false) }) })
public class GeneticAlgorithm extends ParamSpaceExploAlgorithm {

	int populationDim = 3;
	double crossoverProb = 0.7;
	double mutationProb = 0.1;
	int nbPrelimGenerations = 1;
	int maxGenerations = 20;

	Initialization initPop;
	CrossOver crossOverOp;
	Mutation mutationOp;
	Selection selectionOp;
	Boolean improveSolution;

	protected static final String POP_DIM = "pop_dim";
	protected static final String CROSSOVER_PROB = "crossover_prob";
	protected static final String MUTATION_PROB = "mutation_prob";
	protected static final String NB_GEN = "nb_prelim_gen";
	protected static final String MAX_GEN = "max_gen";
	protected static final String IMPROVE_SOL = "improve_sol";
	protected static final String STOCHASTIC_SEL = "stochastic_sel";

	protected Neighborhood neighborhood;

	public GeneticAlgorithm(final IDescription species) {
		super(species);
		initParams();
	}

	@Override
	public void initializeFor(final IScope scope, final BatchAgent agent) throws GamaRuntimeException {
		super.initializeFor(scope, agent);
		final IExpression impSol = getFacet(IMPROVE_SOL);
		if (impSol != null) {
			improveSolution = Cast.asBool(scope, impSol.value(scope));
		}
		if (improveSolution != null && improveSolution) {
			final List<IParameter.Batch> v = agent.getParametersToExplore();
			neighborhood = new Neighborhood1Var(v);
		}
	}

	@Override
	public void initParams(final IScope scope) {
		initPop = new InitializationUniform();
		crossOverOp = new CrossOver1Pt();
		mutationOp = new Mutation1Var();
		final IExpression sts = getFacet(STOCHASTIC_SEL);
		if (sts != null) {
			final Boolean useStoc = Cast.asBool(scope, sts.value(scope));
			if (useStoc != null && useStoc) {
				selectionOp = new SelectionRoulette();
			} else {
				selectionOp = new SelectionBest();
			}
		} else {
			selectionOp = new SelectionBest();
		}

		final IExpression popDim = getFacet(POP_DIM);
		if (popDim != null) {
			populationDim = Cast.asInt(scope, popDim.value(scope));
		}
		final IExpression crossOverPb = getFacet(CROSSOVER_PROB);
		if (crossOverPb != null) {
			crossoverProb = Cast.asFloat(scope, crossOverPb.value(scope));
		}
		final IExpression mutationPb = getFacet(MUTATION_PROB);
		if (mutationPb != null) {
			mutationProb = Cast.asFloat(scope, mutationPb.value(scope));
		}
		final IExpression nbprelimgen = getFacet(NB_GEN);
		if (nbprelimgen != null) {
			nbPrelimGenerations = Cast.asInt(scope, nbprelimgen.value(scope));
		}
		final IExpression maxgen = getFacet(MAX_GEN);
		if (maxgen != null) {
			maxGenerations = Cast.asInt(scope, maxgen.value(scope));
		}
	}

	@Override
	public ParametersSet findBestSolution(final IScope scope) throws GamaRuntimeException {
		final List<IParameter.Batch> variables = currentExperiment.getParametersToExplore();
		setBestFitness(null);
		initializeTestedSolutions();
		List<Chromosome> population = initPop.initializePop(scope, variables, this);
		int nbGen = 1;
		while (nbGen <= maxGenerations) {
			try (ICollector<Chromosome> children = Collector.newSet()) {
				for (final Chromosome chromosome : population) {
					if (scope.getRandom().next() < crossoverProb && !variables.isEmpty()) {
						children.addAll(crossOverOp.crossOver(scope, chromosome,
								population.get(scope.getRandom().between(0, population.size() - 1))));
					}
				}
				population.addAll(children.items());
			}

			try (ICollector<Chromosome> mutatePop = Collector.newSet()) {
				for (final Chromosome chromosome : population) {
					if (scope.getRandom().next() < mutationProb && !variables.isEmpty()) {
						mutatePop.add(mutationOp.mutate(scope, chromosome, variables));
					}
				}
				population.addAll(mutatePop.items());
			}
			computePopFitness(scope, population);
			population = population.stream().distinct().collect(Collectors.toList());
			population = selectionOp.select(scope, population, populationDim, isMaximize());
			nbGen++;
		}
		// DEBUG.LOG("Best solution : " + bestSolution + " fitness : "
		// + bestFitness);
		return getBestSolution();
	}

	private void computePopFitness(final IScope scope, final List<Chromosome> population) throws GamaRuntimeException {
		for (final Chromosome chromosome : population) {
			computeChroFitness(scope, chromosome);
		}
		if (this.improveSolution != null && improveSolution) {
			for (final Chromosome chromosome : population) {
				ParametersSet sol = chromosome.convertToSolution(scope, currentExperiment.getParametersToExplore());
				sol = improveSolution(scope, sol, chromosome.getFitness());
				chromosome.update(scope, sol);
				final double fitness = testedSolutions.get(sol);
				chromosome.setFitness(fitness);
			}
		}
	}

	public void computeChroFitness(final IScope scope, final Chromosome chromosome) {
		final ParametersSet sol = chromosome.convertToSolution(scope, currentExperiment.getParametersToExplore());
		Double fitness = testedSolutions.get(sol);
		if (fitness == null) {
			fitness = currentExperiment.launchSimulationsWithSolution(sol);
		}
		testedSolutions.put(sol, fitness);
		chromosome.setFitness(fitness);
	}

	@Override
	public void addParametersTo(final List<IParameter.Batch> params, final BatchAgent agent) {
		super.addParametersTo(params, agent);
		params.add(new ParameterAdapter("Mutation probability", IExperimentPlan.BATCH_CATEGORY_NAME, IType.FLOAT) {

			@Override
			public Object value() {
				return mutationProb;
			}

		});
		params.add(new ParameterAdapter("Crossover probability", IExperimentPlan.BATCH_CATEGORY_NAME, IType.FLOAT) {

			@Override
			public Object value() {
				return crossoverProb;
			}

		});
		params.add(new ParameterAdapter("Population dimension", IExperimentPlan.BATCH_CATEGORY_NAME, IType.INT) {

			@Override
			public Object value() {
				return populationDim;
			}

		});
		params.add(new ParameterAdapter("Preliminary number of generations", IExperimentPlan.BATCH_CATEGORY_NAME,
				IType.FLOAT) {

			@Override
			public Object value() {
				return nbPrelimGenerations;
			}

		});
		params.add(
				new ParameterAdapter("Max. number of generations", IExperimentPlan.BATCH_CATEGORY_NAME, IType.FLOAT) {

					@Override
					public Object value() {
						return maxGenerations;
					}

				});
	}

	private ParametersSet improveSolution(final IScope scope, final ParametersSet solution,
			final double currentFitness) {
		ParametersSet bestSol = solution;
		double bestFitness = currentFitness;
		while (true) {
			final List<ParametersSet> neighbors = neighborhood.neighbor(scope, solution);
			if (neighbors.isEmpty()) {
				break;
			}
			ParametersSet bestNeighbor = null;

			for (final ParametersSet neighborSol : neighbors) {
				if (neighborSol == null) {
					continue;
				}
				final double neighborFitness;
				if (!testedSolutions.containsKey(neighborSol)) {
					neighborFitness = currentExperiment.launchSimulationsWithSolution(neighborSol);
					testedSolutions.put(neighborSol, neighborFitness);
				} else {
					neighborFitness = testedSolutions.get(neighborSol);
				}

				if (isMaximize() && neighborFitness > bestFitness || !isMaximize() && neighborFitness < bestFitness) {
					bestNeighbor = neighborSol;
					bestFitness = neighborFitness;
					bestSol = bestNeighbor;
				}
			}
			if (bestNeighbor != null) {
				bestSol = bestNeighbor;
			} else {
				break;
			}
		}

		return bestSol;
	}

	public int getPopulationDim() {
		return populationDim;
	}

	public double getMutationProb() {
		return mutationProb;
	}

	public int getNbPrelimGenerations() {
		return nbPrelimGenerations;
	}

	public int getMaxGenerations() {
		return maxGenerations;
	}

	public Mutation getMutationOp() {
		return mutationOp;
	}

}