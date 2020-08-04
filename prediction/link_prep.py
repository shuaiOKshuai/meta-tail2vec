import random
import math

dataset = 'email'


def read_edge():
    fake_edge_test_list = list()
    with open('./link_prediction/' + dataset + '/fake_edge_test.txt', "r") as fr:
        lines = fr.readlines()
        for line in lines:
            temp = list(line.strip('\n').split(' '))
            fake_edge_test_list.append((str(int(temp[0]) - 1), str(int(temp[1]) - 1)))
    fake_edge_task_list = list()
    with open('./link_prediction/' + dataset + '/fake_edge_task.txt', "r") as fr:
        lines = fr.readlines()
        for line in lines:
            temp = list(line.strip('\n').split(' '))
            fake_edge_task_list.append((str(int(temp[0]) - 1), str(int(temp[1]) - 1)))
    true_edge_test_list = list()
    with open('./link_prediction/' + dataset + '/true_edge_test.txt', "r") as fr:
        lines = fr.readlines()
        for line in lines:
            temp = list(line.strip('\n').split(' '))
            true_edge_test_list.append((str(int(temp[0]) - 1), str(int(temp[1]) - 1)))
    true_edge_task_list = list()
    with open('./link_prediction/' + dataset + '/true_edge_task.txt', "r") as fr:
        lines = fr.readlines()
        for line in lines:
            temp = list(line.strip('\n').split(' '))
            true_edge_task_list.append((str(int(temp[0]) - 1), str(int(temp[1]) - 1)))
    return fake_edge_test_list, true_edge_test_list, fake_edge_task_list, true_edge_task_list


def read_emb():
    emb_dict1 = dict()
    with open('./data/' + dataset + '/graph.embeddings', "r") as f:
        lines = f.readlines()
        for line in lines:
            temp = list(line.strip('\n').split(' '))
            if len(temp) == 2:
                continue
            else:
                emb_dict1[str(int(temp[0]) - 1)] = temp[1:]
    emb_dict2 = dict()
    with open('./data/' + dataset + '/result_1.csv', "r") as f:
        lines = f.readlines()
        for line in lines:
            temp = list(line.strip('\n').split(' '))
            emb_dict2[str(int(temp[0]) - 1)] = temp[1:]
    emb_dict3 = dict()
    with open('./data/' + dataset + '/result_3.csv', "r") as f:
        lines = f.readlines()
        for line in lines:
            temp = list(line.strip('\n').split(' '))
            emb_dict3[str(int(temp[0]) - 1)] = temp[1:]
    emb_dict4 = dict()
    with open('./data/' + dataset + '/result_5.csv', "r") as f:
        lines = f.readlines()
        for line in lines:
            temp = list(line.strip('\n').split(' '))
            emb_dict4[str(int(temp[0]) - 1)] = temp[1:]
    return emb_dict1, emb_dict2, emb_dict3, emb_dict4


if __name__ == '__main__':
    fake_edge_test_list, true_edge_test_list, fake_edge_task_list, true_edge_task_list = read_edge()
    emb_dict1, emb_dict2, emb_dict3, emb_dict4 = read_emb()
    data = list()
    data_test = list()
    data_train = list()
    temp_data_test = dict()
    temp_data_train = dict()
    s_node_set = set()
    for e in true_edge_test_list:
        s_node_set.add(e[0])
        temp_data_test[e[0]] = list()
        temp_data_test[e[0]].append(e[1])
    for e in fake_edge_test_list:
        temp_data_test[e[0]].append(e[1])
    for n in temp_data_test.keys():
        temp = list()
        temp.append(n)
        for adj in temp_data_test[n]:
            temp.append(adj)
        data.append(temp)
        data_test.append(temp)

    for e in true_edge_task_list:
        temp_data_train[e[0]] = list()
        temp_data_train[e[0]].append(e[1])
    for e in fake_edge_task_list:
        temp_data_train[e[0]].append(e[1])
    for n in temp_data_train.keys():
        temp = list()
        temp.append(n)
        for adj in temp_data_train[n]:
            temp.append(adj)
        data.append(temp)
        data_train.append(temp)
    with open('./data/' + dataset + '/link.emb', "w") as f_w:
        f_w.write(str(len(emb_dict1)) + ' 128' + '\n')
        for n in emb_dict1:
            if n in s_node_set:
                f_w.write(str(n) + ' ')
                f_w.write(' '.join(emb_dict4[n]))
                f_w.write('\n')
            else:
                f_w.write(str(n) + ' ')
                f_w.write(' '.join(emb_dict1[n]))
                f_w.write('\n')
    with open('./data/' + dataset + '/ground_truth.txt', "w") as f_w_1:
        for l in data:
            f_w_1.write(l[0] + '\t' + l[1] + '\n')
    num_splits = 10
    index = 0
    t_list = list()
    f_list = list()
    for p in range(num_splits):
        random.seed(p + 19)
        random.shuffle(data_train)
        i = int(math.ceil(len(data_train) * 0.5))
        with open('./data/' + dataset + '/train%s' % str(p + 1), "w") as f_w_2:
            while index < i:
                for j in range(len(data_train[index])):
                    if j > 0:
                        if j == 1:
                            t_list.append(data_train[index][j])
                        else:
                            f_list.append(data_train[index][j])
                for k in t_list:
                    for h in f_list:
                        f_w_2.write(data_train[index][0])
                        f_w_2.write('\t' + k + '\t' + h)
                        f_w_2.write('\n')
                    t_list.clear()
                    f_list.clear()
                index += 1
        with open('./data/' + dataset + '/test%s' % str(p + 1), "w") as f_w_3:
            index = 0
            while index < len(data_test):
                temp = list()
                for j in range(len(data_test[index])):
                    if j > 0:
                        temp.append(data_test[index][j])
                random.seed(index)
                random.shuffle(temp)
                f_w_3.write(data_test[index][0])
                for k in temp:
                    f_w_3.write('\t' + k)
                f_w_3.write('\n')
                temp.clear()
                index += 1
        index = 0
