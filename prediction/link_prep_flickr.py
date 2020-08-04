from sklearn.model_selection import train_test_split
import copy
import random

dataset = 'flickr'
node_info_dir = './dataset/' + dataset + '/nodes.csv'
edge_info_dir = './dataset/' + dataset + '/edges.csv'
class_info_dir = './dataset/' + dataset + '/groups.csv'
node_class_info_dir = './dataset/' + dataset + '/group-edges.csv'
train_node_range = (36, 56)
sparse_node_threshold = 5

set_1 = set()
set_2 = set()
set_3 = set()
set_4 = set()
set_5 = set()

lessequal5 = set()
set_between_5_10 = set()
set_between_10_20 = set()
set_between_20_40 = set()
set_between_40_100 = set()
larger100 = set()


def read_flickr_graph():
    node_dict = dict()
    graph_dict = dict()
    class_dict = dict()
    node_class = dict()
    node_index = 1
    with open(node_info_dir, "r") as fr:
        lines = fr.readlines()
        for line in lines:
            temp = list(line.strip('\n').split(' '))
            node_dict[temp[0]] = node_index
            node_index += 1
    # reverse_node_dict = dict(zip(node_dict.values(), node_dict.keys()))
    with open(node_info_dir, "r") as fr:
        lines = fr.readlines()
        for line in lines:
            temp = list(line.strip('\n').split(' '))
            if node_dict[temp[0]] not in graph_dict.keys():
                graph_dict[node_dict[temp[0]]] = set()
    edge_num = 0
    with open(edge_info_dir, "r") as fr:
        lines = fr.readlines()
        for line in lines:
            edge_num += 1
            temp = list(line.strip('\n').split(','))
            graph_dict[node_dict[temp[0]]].add(node_dict[temp[1]])
            graph_dict[node_dict[temp[1]]].add(node_dict[temp[0]])
    class_index = 0
    with open(class_info_dir, "r") as fr:
        lines = fr.readlines()
        for line in lines:
            temp = list(line.strip('\n').split(' '))
            if temp[0] not in class_dict.keys():
                class_dict[temp[0]] = class_index
                class_index += 1
    with open(node_class_info_dir, "r") as fr:
        lines = fr.readlines()
        for line in lines:
            temp = list(line.strip('\n').split(','))
            if node_dict[temp[0]] not in node_class.keys():
                node_class[node_dict[temp[0]]] = set()
                node_class[node_dict[temp[0]]].add(class_dict[temp[1]])
            else:
                node_class[node_dict[temp[0]]].add(class_dict[temp[1]])
    print('dataset ' + dataset)
    print('total node number: %d' % len(graph_dict.keys()))
    print('total edge number: %d' % edge_num)
    print('total class number: %d' % len(class_dict.keys()))
    return node_dict, graph_dict, node_class, class_dict


def select_sparse_node(graph_dict):
    sparse_node_set = set()
    link6_set = set()
    num_lessequal_5 = 0
    num_between_5_10 = 0
    num_between_10_20 = 0
    num_between_20_40 = 0
    num_between_40_100 = 0
    num_more_100 = 0
    for i in graph_dict.keys():
        if len(graph_dict[i]) == 6:
            link6_set.add(i)
        if len(graph_dict[i]) <= 5:
            num_lessequal_5 += 1
        if 5 < len(graph_dict[i]) <= 10:
            num_between_5_10 += 1
        if 10 < len(graph_dict[i]) <= 20:
            num_between_10_20 += 1
        if 20 < len(graph_dict[i]) <= 40:
            num_between_20_40 += 1
        if 40 < len(graph_dict[i]) <= 100:
            num_between_40_100 += 1
        if len(graph_dict[i]) > 100:
            num_more_100 += 1
        if len(graph_dict[i]) <= sparse_node_threshold:
            sparse_node_set.add(i)
    print('edge num <= 5: %d' % num_lessequal_5)
    print('5 < edge num <= 10: %d' % num_between_5_10)
    print('10 < edge num <= 20: %d' % num_between_10_20)
    print('20 < edge num <= 40: %d' % num_between_20_40)
    print('40 < edge num <= 100: %d' % num_between_40_100)
    print('100 < edge num: %d' % num_more_100)
    print('sparse node num is %d' % len(sparse_node_set))
    print('=================================')
    return sparse_node_set, link6_set


def node_type_info(graph_dict, sparse_node_set):
    node_type_dict = dict()
    for n in graph_dict.keys():
        if n in sparse_node_set:
            node_type_dict[n] = 'sparse'
        else:
            neighbor = len(graph_dict[n])
            for adj in graph_dict[n]:
                if adj in sparse_node_set:
                    neighbor -= 1
            if neighbor > sparse_node_threshold:
                node_type_dict[n] = 'dense'
            else:
                node_type_dict[n] = 'middle'
    s_num = 0
    d_num = 0
    m_num = 0
    for n in node_type_dict.keys():
        if node_type_dict[n] == 'sparse':
            s_num += 1
        if node_type_dict[n] == 'dense':
            d_num += 1
        if node_type_dict[n] == 'middle':
            m_num += 1
    print('sparse node num is %d' % s_num)
    print('dense node num is %d' % d_num)
    print('middle node num is %d' % m_num)
    print('=================================')
    return node_type_dict


def generate_train_test_candidate(graph_dict, sparse_node_set, node_type_dict, link6_set):
    train_node_candidate_set = set()
    task_node_candidate_set = set()
    test_node_candidate_set_1 = set()
    test_node_candidate_set_2 = set()
    test_node_candidate_set_3 = set()
    test_node_candidate_set_4 = set()
    test_node_candidate_set_5 = set()
    test_nodes_list = list()
    test_node_candidate_num = 0
    for n in sparse_node_set:
        dense_num = 0
        middle_num = 0
        sparse_num = 0
        for adj in graph_dict[n]:
            if node_type_dict[adj] == 'dense':
                dense_num += 1
            elif node_type_dict[adj] == 'middle':
                middle_num += 1
            else:
                sparse_num += 1
        if middle_num == 0 and sparse_num == 0:
            if len(graph_dict[n]) == 1:
                test_node_candidate_set_1.add(n)
            if len(graph_dict[n]) == 2:
                test_node_candidate_set_2.add(n)
            if len(graph_dict[n]) == 3:
                test_node_candidate_set_3.add(n)
            if len(graph_dict[n]) == 4:
                test_node_candidate_set_4.add(n)
            if len(graph_dict[n]) == 5:
                test_node_candidate_set_5.add(n)
            test_node_candidate_num += 1
    print(len(test_node_candidate_set_1))
    print(len(test_node_candidate_set_2))
    print(len(test_node_candidate_set_3))
    print(len(test_node_candidate_set_4))
    print(len(test_node_candidate_set_5))
    print(test_node_candidate_num)
    for n in graph_dict.keys():
        if n in sparse_node_set:
            continue
        else:
            if node_type_dict[n] == 'middle':
                continue
            else:
                neighbor = len(graph_dict[n])
                for adj in graph_dict[n]:
                    if adj in sparse_node_set:
                        neighbor -= 1
                if 6 < neighbor:
                    task_node_candidate_set.add(n)
                if train_node_range[0] < neighbor <= train_node_range[1]:
                    train_node_candidate_set.add(n)
    test_nodes_list_2 = list(test_node_candidate_set_2)
    test_nodes_list_3 = list(test_node_candidate_set_3)
    test_nodes_list_4 = list(test_node_candidate_set_4)
    test_nodes_list_5 = list(test_node_candidate_set_5)
    test_nodes_list.extend(test_nodes_list_2)
    test_nodes_list.extend(test_nodes_list_3)
    test_nodes_list.extend(test_nodes_list_4)
    test_nodes_list.extend(test_nodes_list_5)
    new_link6_set = set()
    for n in link6_set:
        flag = 0
        for adj in graph_dict[n]:
            if adj in test_nodes_list:
                flag = 1
        if flag == 0:
            new_link6_set.add(n)
    # test_nodes_list_6, _, _, _ = train_test_split(list(new_link6_set), range(len(new_link6_set)), train_size=17,
    #                                               random_state=19)
    test_nodes_list_6 = list(new_link6_set)
    test_nodes_list.extend(test_nodes_list_6)
    test_node_candidate_set = set(test_nodes_list)
    task_node_candidate_set, _, _, _ = train_test_split(list(task_node_candidate_set),
                                                        range(len(task_node_candidate_set)),
                                                        train_size=2 * len(test_node_candidate_set),
                                                        random_state=19)
    print('train node candidate num is %d' % len(train_node_candidate_set))
    print('test node candidate num is %d' % len(test_node_candidate_set))
    print('task node candidate num is %d' % len(task_node_candidate_set))
    print('=================================')
    return train_node_candidate_set, test_node_candidate_set, task_node_candidate_set


def generate_train_test_set(train_node_candidate_set, test_node_candidate_set):
    # train_nodes_array = np.random.choice(list(train_node_candidate_set), train_num, replace=False)
    # train_node_set = set(train_nodes_array.tolist())
    train_node_set = train_node_candidate_set
    # test_nodes_list, _, _, _ = train_test_split(list(test_node_candidate_set), range(len(test_node_candidate_set)),
    #                                             train_size=test_num, random_state=19)
    # test_nodes_array = np.random.choice(list(test_node_candidate_set), test_num, replace=False)
    # test_node_set = set(test_nodes_list)
    return train_node_set, test_node_candidate_set


def select_remove_edge(graph_dict, test_node_candidate_set, task_node_candidate_set):
    true_edge_test_set = set()
    true_edge_task_set = set()
    graph_r_dict = copy.deepcopy(graph_dict)
    for n in test_node_candidate_set:
        tail_node, _, _, _ = train_test_split(list(graph_dict[n]), range(len(graph_dict[n])), train_size=1,
                                              random_state=22)
        if tail_node[0] in graph_r_dict[n]:
            true_edge_test_set.add((n, tail_node[0]))
            graph_r_dict[n].remove(tail_node[0])
            graph_r_dict[tail_node[0]].remove(n)
    for n in task_node_candidate_set:
        tail_node, _, _, _ = train_test_split(list(graph_dict[n]), range(len(graph_dict[n])), train_size=1,
                                              random_state=21)
        while tail_node[0] == n:
            tail_node, _, _, _ = train_test_split(list(graph_dict[n]), range(len(graph_dict[n])), train_size=1,
                                                  random_state=20)
        if tail_node[0] in graph_r_dict[n]:
            true_edge_task_set.add((n, tail_node[0]))
            graph_r_dict[n].remove(tail_node[0])
            graph_r_dict[tail_node[0]].remove(n)
    return graph_r_dict, true_edge_test_set, true_edge_task_set


def select_fake_edge(graph_dict, test_node_candidate_set, task_node_candidate_set):
    fake_edge_test_set = set()
    fake_edge_task_set = set()
    for n in test_node_candidate_set:
        for i in range(5):
            tail_node = random.sample(list(graph_dict.keys()), 1)[0]
            while tail_node == n or tail_node in graph_dict[n] or (n, tail_node) in fake_edge_test_set:
                tail_node = random.sample(list(graph_dict.keys()), 1)[0]
            fake_edge_test_set.add((n, tail_node))
    for n in task_node_candidate_set:
        for i in range(5):
            tail_node = random.sample(list(graph_dict.keys()), 1)[0]
            while tail_node == n or tail_node in graph_dict[n] or (n, tail_node) in fake_edge_task_set:
                tail_node = random.sample(list(graph_dict.keys()), 1)[0]
            fake_edge_task_set.add((n, tail_node))
    return fake_edge_test_set, fake_edge_task_set


def save_graph(graph_dict, graph_dir):
    with open(graph_dir, 'w') as fw:
        for key in graph_dict.keys():
            fw.write(str(key))
            for item in graph_dict[key]:
                fw.write(' ' + str(item))
            fw.write('\n')


def save_node(node_set, node_save_dir):
    with open(node_save_dir, 'w') as fw:
        for n in node_set:
            fw.write(str(n))
            fw.write('\n')


def save_edge(edge_set, edge_save_dir):
    with open(edge_save_dir, 'w') as fw:
        for n in edge_set:
            fw.write(str(n[0]) + ' ' + str(n[1]) + '\n')


def save_dense_graph(graph_dict, graph_dir, sparse_node_set, test_node_set):
    avoid_set = sparse_node_set | test_node_set
    with open(graph_dir, 'w') as fw:
        for key in graph_dict.keys():
            fw.write(str(key))
            for item in graph_dict[key]:
                if item not in avoid_set:
                    fw.write(' ' + str(item))
            fw.write('\n')


def save_node_class_info(node_info, save_dir):
    with open(save_dir, 'w') as fw:
        for key in node_info.keys():
            fw.write(str(key))
            for item in node_info[key]:
                fw.write(' ' + str(item))
            fw.write('\n')


if __name__ == '__main__':
    node2id, graph, node_class_info, class2id = read_flickr_graph()
    sparse_node, link6 = select_sparse_node(graph)
    node_type = node_type_info(graph, sparse_node)
    train_node_candidate, test_node_candidate, task_node_candidate = generate_train_test_candidate(graph, sparse_node,
                                                                                                   node_type, link6)
    train_node, test_node = generate_train_test_set(train_node_candidate, test_node_candidate)
    graph_r, true_edge_test, true_edge_task = select_remove_edge(graph, test_node_candidate, task_node_candidate)
    fake_edge_test, fake_edge_task = select_fake_edge(graph, test_node_candidate, task_node_candidate)
    save_node(train_node, './link_prediction/' + dataset + '/train.csv')
    save_node(test_node, './link_prediction/' + dataset + '/test.csv')
    save_node_class_info(node_class_info, './link_prediction/' + dataset + '/node_class.txt')
    save_graph(graph_r, './link_prediction/' + dataset + '/graph.adjlist')
    save_dense_graph(graph_r, './link_prediction/' + dataset + '/graph_dense.adjlist', sparse_node, test_node)
    save_edge(fake_edge_test, './link_prediction/' + dataset + '/fake_edge_test.txt')
    save_edge(true_edge_test, './link_prediction/' + dataset + '/true_edge_test.txt')
    save_edge(fake_edge_task, './link_prediction/' + dataset + '/fake_edge_task.txt')
    save_edge(true_edge_task, './link_prediction/' + dataset + '/true_edge_task.txt')
