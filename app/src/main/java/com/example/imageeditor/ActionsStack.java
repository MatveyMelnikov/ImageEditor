package com.example.imageeditor;

import java.util.Stack;

public class ActionsStack {
    private final Stack<ImageAction> stack;
    private final int fixedSize;
    public int currentIndex = -1;

    public ActionsStack(int fixedSize) {
        stack = new Stack<ImageAction>();
        this.fixedSize = fixedSize;
    }

    public ImageAction pop() {
        return stack.pop();
    }

    public ImageAction peekWithPosition() {
        if (currentIndex >= stack.size() || currentIndex < 0)
            return null;
        else
            return stack.get(currentIndex--);
    }

    public ImageAction reversePeekWithPosition() {
        if (currentIndex + 1 >= stack.size() || currentIndex + 1 < 0) {
            return null;
        }
        else {
            currentIndex++;
            return stack.get(currentIndex);
        }
    }

    public boolean isStartPosition() {
        return (currentIndex == (stack.size() - 1) || currentIndex == -1);
    }

    public void push(ImageAction imageAction) {
        long count = stack.stream().filter(item ->
                (item.x == imageAction.x && item.y == imageAction.y &&
                        item.isActivated == imageAction.isActivated)
        ).count();
        if (count != 0)
            return;

        if (stack.size() >= fixedSize) {
            stack.removeElementAt(0);
        }
        else
            currentIndex++;
        stack.push(imageAction);
    }

    public void clear() {
        currentIndex = -1;
        stack.clear();
    }
}
