# meta-tail2vec

# Towards Locality-Aware Meta-Learning of Tail Node Embeddings on Networks

This repository is the official implementation of [Towards Locality-Aware Meta-Learning of Tail Node Embeddings on Networks](). 

## Requirements

To install requirements:

```setup
pip install -r requirements.txt
```

To download datasets:
[Wiki](https://github.com/thunlp/OpenNE/tree/master/data/wiki).
[Flickr](http://socialcomputing.asu.edu/datasets/Flickr).
[Email](https://snap.stanford.edu/data/email-Eu-core.html).

## Repository Structure
- meta-tail2vec/:
	- ./prep_flickr.py or prep_email.py or prep_wiki.py: Prepare necessary data for data_generator.py.
	- ./data_generator.py: Generate pipeline for the model, specifically the meta-training and meta-testing tasks.
	- ./main.py: The main entrance of the model. You can adjust training batch number and pipeline data batch number here.
	- ./maml.py: The MAML framework.
	- ./multiclass_task.py: The task script for email and wiki dataset.
	- ./multilabel_task.py: The task script for flickr dataset.
- meta-tail2vec/prediction/:
	- ./prep_flickr_link.py or prep_email_link.py or prep_wiki_link.py: Prepare necessary data for link prediction.
	- ./link_prep.py: Prepare necessary data for link prediction.
	- example command: java -cp lib/* -Dconfig=config -Dsplits=10 -Demb=facebook_emb.txt -Dgt=facebook_gt.txt -Dtest=facebook_test -Dtrain=facebook_train -Dpred=facebook_pred exec.LearnEmb
	- Dconfig=config Hyper parameters of L2R model.
	- Dsplits=<INT> Different splits number of train/test (see -Dtest -Dtrain below).
	- Demb=<filename> embedding file，the first line needs to be "node num, dim".
	- Dgt=<filename> Ground truth file.
	- Dtest=<filename> Test file name + *prefix*, the prefix can be 1 or 2 or 3 which represents different splits. If  -Dsplit=10, code will automaticlly load 1-10.
	- Dtrain=<filename> Train file name + *prefix*, the same with Dtest.
	- Dpred=<filename> output file in order, each query in test also in this file, but maybe not the same row.
- meta-tail2vec/dataset/:
    - ./flickr/: Flcikr dataset.
    - ./email/: Email dataset.
    - ./wiki/: Wiki dataset.

## Training

To train the model in the paper:

First please run deepwalk or other method as base embedding model, put them into data/xxx/, the embedding format is the same as deepwalk output.

Classification:

```classification
python prep_flickr.py
python main.py
```

Prediction:
```prediction
python prep_flickr_link.py
python main.py
```

## Evaluation

To evaluate the model

Classification:

```classification
python multilabel_task.py
```

Prediction:

First use link_prep.py to generate L2R data, then use java program in /prediction to evaluate the embedding.

```prediction
python ./link_prep.py
java -cp lib/* -Dconfig=config -Dsplits=10 -Demb=facebook_emb.txt -Dgt=facebook_gt.txt -Dtest=facebook_test -Dtrain=facebook_train -Dpred=facebook_pred exec.LearnEmb
```


