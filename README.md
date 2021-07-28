# meta-tail2vec

# Towards Locality-Aware Meta-Learning of Tail Node Embeddings on Networks

This repository is the implementation (in tensorflow) of our paper [Towards Locality-Aware Meta-Learning of Tail Node Embeddings on Networks](https://zemin-liu.github.io/papers/CIKM-20-towards-locality-aware-meta-learning-of-tail-node-embeddings-on-network.pdf), which is published in CIKM-2020.

## Requirements

To install requirements:

```setup
pip install -r requirements.txt
```

To download the original datasets:
[Wiki](https://github.com/thunlp/OpenNE/tree/master/data/wiki).
[Flickr](http://socialcomputing.asu.edu/datasets/Flickr).
[Email](https://snap.stanford.edu/data/email-Eu-core.html).

## Repository Structure
- meta-tail2vec/:
	- dataset/: original dataset without any processing
	- data/: processed data, including train / test data splitting 
	- prep_dataset.py: Prepare necessary data for data_generator.py. When you use different datasets, remember to change the dataset name in line 3.
	- data_generator.py: Generate pipeline for the model, specifically the meta-training and meta-testing tasks.
	- main.py: The main entrance of the model. You can adjust training batch number and pipeline data directory, dataset name, number shots etc in line 65--70.
	- maml.py: The MAML framework.
	- multiclass_task.py: example code for the downstream task of node classification (multi-class setting) and evaluation
	- multilabel_task.py: example code for the downstream task of node classification (multi-label setting) and evaluation
	- prediction/: data processing code for link prediction. It requires different processing of the original datasets, as we need to remove some links from the original graph for testing.

## Train

To train the model in the paper:

First please run deepwalk or other method as base embedding model, the embedding format is the same as deepwalk output.

```
python prep_dataset.py
python main.py
```

## Citation

	@inproceedings{liu2020towards,
		title={Towards locality-aware meta-learning of tail node embeddings on networks},
		author={Liu, Zemin and Zhang, Wentao and Fang, Yuan and Zhang, Xinming and Hoi, Steven CH},
		booktitle={Proceedings of the 29th ACM International Conference on Information & Knowledge Management},
		year={2020}
	}
