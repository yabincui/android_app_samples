#include <jni.h>
#include <stdio.h>
#include <stdlib.h>

#include <android/log.h>

#include <string>
#include <vector>

#define LOG_TAG "sudoGameJNI"
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)


static const int BOARD_ROWS = 9;
static const int BOARD_COLS = 9;

struct Board {
    unsigned char digits[BOARD_ROWS][BOARD_COLS];
    bool fixed[BOARD_ROWS][BOARD_COLS];
};

static bool getBoardFromObjectArray(JNIEnv* env, jobjectArray objArray, Board& board) {
    if (env->GetArrayLength(objArray) != BOARD_ROWS) {
        return false;
    }
    jclass intArrayCls = env->FindClass("[I");
    if (intArrayCls == NULL) {
        return false;
    }
    for (int i = 0; i < BOARD_ROWS; ++i) {
        jobject obj = env->GetObjectArrayElement(objArray, i);
        if (!env->IsInstanceOf(obj, intArrayCls)) {
            return false;
        }
        if (env->GetArrayLength((jintArray)obj) != BOARD_COLS) {
            return false;
        }
        jint* values = env->GetIntArrayElements((jintArray)obj, NULL);
        for (int j = 0; j < BOARD_COLS; ++j) {
            int c = values[j];
            if (c < -9 || c > 9) {
                return false;
            }
            board.digits[i][j] = (c > 0 ? -c : c);
            board.fixed[i][j] = (c < 0);
        }
        env->ReleaseIntArrayElements((jintArray)obj, values, 0);
        env->DeleteLocalRef(obj);
    }
    env->DeleteLocalRef(intArrayCls);
    return true;
}

static bool setBoardToObjectArray(JNIEnv* env, jobjectArray* pObjArray, const Board& board) {
    jclass intArrayCls = env->FindClass("[I");
    if (intArrayCls == NULL) {
        return false;
    }
    jobjectArray objArray = env->NewObjectArray(BOARD_ROWS, intArrayCls, NULL);
    if (objArray == NULL) {
        env->DeleteLocalRef(intArrayCls);
        return false;
    }
    for (int i = 0; i < BOARD_COLS; ++i) {
        jintArray intArray = env->NewIntArray(BOARD_COLS);
        if (intArray == NULL) {
            goto errReleaseObjArray;
        }
        env->SetObjectArrayElement(objArray, i, intArray);
        env->DeleteLocalRef(intArray);
    }
    for (int i = 0; i < BOARD_ROWS; ++i) {
        jintArray intArray = (jintArray)env->GetObjectArrayElement(objArray, i);
        jint* values = env->GetIntArrayElements(intArray, NULL);
        for (int j = 0; j < BOARD_COLS; ++j) {
            int c = board.digits[i][j];
            if (c < -9 || c > 9) {
                goto errReleaseObjArray;
            }
            values[j] = (board.fixed[i][j] ? -c : c);
        }
        env->ReleaseIntArrayElements(intArray, values, 0);
        values = env->GetIntArrayElements(intArray, NULL);
        env->ReleaseIntArrayElements(intArray, values, 0);
        env->DeleteLocalRef(intArray);
    }
    env->DeleteLocalRef(intArrayCls);
    *pObjArray = objArray;
    return true;

    errReleaseObjArray:
        env->DeleteLocalRef(objArray);
        env->DeleteLocalRef(intArrayCls);
        return false;
}

static bool checkValid(const Board& board, int curR, int curC) {
    int digit = board.digits[curR][curC];
    for (int r = 0; r < BOARD_ROWS; ++r) {
        if (r == curR) {
            continue;
        }
        if (board.digits[r][curC] == digit) {
            return false;
        }
    }
    for (int c = 0; c < BOARD_COLS; ++c) {
        if (c == curC) {
            continue;
        }
        if (board.digits[curR][c] == digit) {
            return false;
        }
    }
    int startR = curR / 3 * 3;
    int startC = curC / 3 * 3;
    for (int r = startR; r < startR + 3; ++r) {
        for (int c = startC; c < startC + 3; ++c) {
            if (r == curR && c == curC) {
                continue;
            }
            if (board.digits[r][c] == digit) {
                return false;
            }
        }
    }
    return true;
}

static bool canFindSolution_r(Board& board, int curR, int curC) {
    while (curR < BOARD_ROWS) {
        int nextR = curR;
        int nextC = curC + 1;
        if (nextC == BOARD_COLS) {
            nextR++;
            nextC = 0;
        }
        if (board.digits[curR][curC] == 0) {
            for (int guess = 1; guess <= 9; ++guess) {
                board.digits[curR][curC] = guess;
                if (checkValid(board, curR, curC)) {
                    if (canFindSolution_r(board, nextR, nextC)) {
                        return true;
                    }
                }
            }
            board.digits[curR][curC] = 0;
            return false;
        } else {
            curR = nextR;
            curC = nextC;
        }
    }
    return true;
}

static bool canFindSolution(Board& board) {
    for (int r = 0; r < BOARD_ROWS; ++r) {
        for (int c = 0; c < BOARD_COLS; ++c) {
            if (board.digits[r][c] == 0) {
                continue;
            }
            if (!checkValid(board, r, c)) {
                return false;
            }
        }
    }
    return canFindSolution_r(board, 0, 0);
}

static void randomBoard(Board& board, int fixedCount) {
    for (int r = 0; r < BOARD_ROWS; ++r) {
        for (int c = 0; c < BOARD_COLS; ++c) {
            board.digits[r][c] = 0;
            board.fixed[r][c] = false;
        }
    }
    fixedCount = std::min(fixedCount, BOARD_ROWS * BOARD_COLS);
    while (fixedCount > 0) {
        int r = rand() % BOARD_ROWS;
        int c = rand() % BOARD_COLS;
        int digit = rand() % 9 + 1;
        if (board.digits[r][c] != 0) {
            continue;
        }
        board.digits[r][c] = digit;
        board.fixed[r][c] = true;
        if (!checkValid(board, r, c)) {
            board.digits[r][c] = 0;
            board.fixed[r][c] = false;
            continue;
        }
        fixedCount--;
    }
}

static bool isPointReasonable(Board& board, int curR, int curC) {
    int possibleCount = 0;
    int oldDigit = board.digits[curR][curC];
    for (int digit = 1; digit <= 9; ++digit) {
        board.digits[curR][curC]  = digit;
        if (checkValid(board, curR, curC)) {
            possibleCount++;
        }
    }
    board.digits[curR][curC] = oldDigit;
    return (possibleCount == 1);
}

static void makeBoardReasonable(Board& nativeBoard) {
    Board experimentBoard;
    int fixedCount = 0;
    for (int r = 0; r < BOARD_ROWS; ++r) {
        for (int c = 0; c < BOARD_COLS; ++c) {
            experimentBoard.digits[r][c] = nativeBoard.digits[r][c];
            experimentBoard.fixed[r][c] = true;
            if (nativeBoard.fixed[r][c]) {
                fixedCount++;
            }
        }
    }
    int leftRemoveCount = BOARD_ROWS * BOARD_COLS - fixedCount;
    while (leftRemoveCount > 0) {
        int tries;
        for (tries = 0; tries < BOARD_ROWS * BOARD_COLS; ++tries) {
            int selR = rand() % BOARD_ROWS;
            int selC = rand() % BOARD_COLS;
            if (experimentBoard.digits[selR][selC] == 0 || nativeBoard.fixed[selR][selC]) {
                continue;
            }
            if (isPointReasonable(experimentBoard, selR, selC)) {
                LOGI("point %d, %d is reasonable", selR, selC);
                experimentBoard.digits[selR][selC] = 0;
                experimentBoard.fixed[selR][selC] = false;
                leftRemoveCount++;
                break;
            }
        }
        if (tries == BOARD_ROWS * BOARD_COLS) {
            break;
        }
    }
    for (int r = 0; r < BOARD_ROWS; ++r) {
        for (int c = 0; c < BOARD_COLS; ++c) {
            nativeBoard.digits[r][c] = experimentBoard.digits[r][c];
            nativeBoard.fixed[r][c] = experimentBoard.fixed[r][c];
        }
    }
}

/*
 * Class:     com_example_yabinc_sudogame_GameModel
 * Method:    initRandomBoard
 * Signature: (IZ)[[I
 */
extern "C" JNIEXPORT jobjectArray JNICALL Java_com_example_yabinc_sudogame_GameModel_initRandomBoard
  (JNIEnv * env, jobject obj, jint fixedCount, jboolean isSolutionReasonable) {
    Board nativeBoard;
    LOGI("call initRandomBoard");
    for (int tries = 0; tries < 1000; ++tries) {
        LOGI("randomBoard, tries = %d", tries);
        randomBoard(nativeBoard, fixedCount);
        LOGI("canFindSolution, tries = %d", tries);
        if (canFindSolution(nativeBoard)) {
            LOGI("success");
            if (isSolutionReasonable) {
                makeBoardReasonable(nativeBoard);
            }
            for (int r = 0; r < BOARD_ROWS; ++r) {
                for (int c = 0; c < BOARD_COLS; ++c) {
                    LOGI("board[%d][%d] = %d", r, c, nativeBoard.digits[r][c]);
                }
            }
            jobjectArray objArray;
            if (!setBoardToObjectArray(env, &objArray, nativeBoard)) {
                return NULL;
            }
            return objArray;
        }
    }
    LOGI("can't initRandomBoard for fixedCount = %d, isSolutionReasonable = %d", fixedCount, isSolutionReasonable);
    return NULL;
}

/*
 * Class:     com_example_yabinc_sudogame_GameModel
 * Method:    canFindSolution
 * Signature: ([[I)Z
 */
extern "C" JNIEXPORT jboolean JNICALL Java_com_example_yabinc_sudogame_GameModel_canFindSolution
  (JNIEnv *env, jobject obj, jobjectArray board) {
    Board nativeBoard;
    if (!getBoardFromObjectArray(env, board, nativeBoard)) {
        LOGE("failed to getBoardFromObjectArray");
        return JNI_FALSE;
    }
    bool ret = canFindSolution(nativeBoard);
    return ret ? JNI_TRUE : JNI_FALSE;
}

/*
 * Class:     com_example_yabinc_sudogame_GameModel
 * Method:    findConflictPairs
 * Signature: ([[I)[I
 */
extern "C" JNIEXPORT jintArray JNICALL Java_com_example_yabinc_sudogame_GameModel_findConflictPairs
  (JNIEnv *env, jobject obj, jobjectArray board) {
    Board nativeBoard;
    if (!getBoardFromObjectArray(env, board, nativeBoard)) {
        LOGE("failed to getBoardFromObjectArray");
        return NULL;
    }
    std::vector<int> conflictPairs;
    for (int r = 0; r < BOARD_ROWS; ++r) {
        for (int c = 0; c < BOARD_COLS; ++c) {
            if (nativeBoard.digits[r][c] == 0) {
                continue;
            }
            int digit = nativeBoard.digits[r][c];
            for (int nr = r + 1; nr < BOARD_ROWS; ++nr) {
                if (nativeBoard.digits[nr][c] == digit) {
                    conflictPairs.push_back(r);
                    conflictPairs.push_back(c);
                    conflictPairs.push_back(nr);
                    conflictPairs.push_back(c);
                }
            }
            for (int nc = c + 1; nc < BOARD_COLS; ++nc) {
                if (nativeBoard.digits[r][nc] == digit) {
                    conflictPairs.push_back(r);
                    conflictPairs.push_back(c);
                    conflictPairs.push_back(r);
                    conflictPairs.push_back(nc);
                }
            }
            int startR = r / 3 * 3;
            int startC = c / 3 * 3;
            for (int nr = startR; nr < startR + 3; ++nr) {
                for (int nc = startC; nc < startC + 3; ++nc) {
                    if (nr < r || (nr == r && nc <= c)) {
                        continue;
                    }
                    if (nativeBoard.digits[nr][nc] == digit) {
                        conflictPairs.push_back(r);
                        conflictPairs.push_back(c);
                        conflictPairs.push_back(nr);
                        conflictPairs.push_back(nc);
                    }
                }
            }
        }
    }
    jintArray result = env->NewIntArray(conflictPairs.size());
    if (result == NULL) {
        return NULL;
    }
    jint* intArray = env->GetIntArrayElements(result, NULL);
    if (intArray == NULL) {
        return NULL;
    }
    for (size_t i = 0; i < conflictPairs.size(); ++i) {
        intArray[i] = conflictPairs[i];
    }
    env->ReleaseIntArrayElements(result, intArray, 0);
    return result;
}

/*
 * Class:     com_example_yabinc_sudogame_GameModel
 * Method:    getOneReasonablePosition
 * Signature: ([[I)[I
 */
extern "C" JNIEXPORT jintArray JNICALL Java_com_example_yabinc_sudogame_GameModel_getOneReasonablePosition
        (JNIEnv *env, jobject obj, jobjectArray board) {
    Board nativeBoard;
    if (!getBoardFromObjectArray(env, board, nativeBoard)) {
        LOGE("failed to getBoardFromObjectArray");
        return NULL;
    }
    std::vector<int> poses;
    for (int r = 0; r < BOARD_ROWS; ++r) {
        for (int c = 0; c < BOARD_COLS; ++c) {
            if (nativeBoard.digits[r][c] == 0 && isPointReasonable(nativeBoard, r, c)) {
                poses.push_back(r);
                poses.push_back(c);
            }
        }
    }
    if (poses.empty()) {
        return NULL;
    }
    int sel = (rand() % poses.size()) & ~1;
    jintArray intArray = env->NewIntArray(2);
    if (intArray == NULL) {
        return NULL;
    }
    int* values = env->GetIntArrayElements(intArray, NULL);
    if (values == NULL) {
        return NULL;
    }
    values[0] = poses[sel];
    values[1] = poses[sel + 1];
    env->ReleaseIntArrayElements(intArray, values, 0);
    return intArray;
}