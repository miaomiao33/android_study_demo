//
// Created by Machenike on 2026/8/18.
//

#ifndef ANDROID_STUDY_DEMO_PROJECT_DOUBLEQUEUE_H
#define ANDROID_STUDY_DEMO_PROJECT_DOUBLEQUEUE_H

#ifdef __cplusplus
extern "C" {
#endif
int create_queue();
int destroy_queue();
int queue_is_empty();
int queue_size();
void *queue_get(int index);
void *queue_get_first();
void *queue_get_last();
int queue_insert(int index, void *pval);
int queue_insert_first(void *pval);
int queue_append_last(void *pval);
int queue_delete(int index);
int queue_delete_first();
int queue_delete_last();
#ifdef __cplusplus
}
#endif

#endif //ANDROID_STUDY_DEMO_PROJECT_DOUBLEQUEUE_H
