import tkinter as tk
from tkinter import ttk, filedialog, messagebox, scrolledtext
import os
import json

class LocalizationEditor:
    def __init__(self, root):
        self.root = root
        self.root.title("JSON 本地化对照编辑器")
        self.root.geometry("1200x700")

        # 存储三个目录的路径
        self.dir_vars = [tk.StringVar(), tk.StringVar(), tk.StringVar()]
        # 同名文件列表
        self.common_files = []
        self.current_index = -1

        # ----- 目录选择区域 -----
        dir_frame = ttk.LabelFrame(self.root, text="选择三个本地化目录", padding=10)
        dir_frame.pack(fill=tk.X, padx=10, pady=5)

        labels = ["目录 1 (如 zh_cn):", "目录 2 (如 en_us):", "目录 3 (如 zh_tw):"]
        for i in range(3):
            row = ttk.Frame(dir_frame)
            row.pack(fill=tk.X, pady=2)
            ttk.Label(row, text=labels[i], width=22).pack(side=tk.LEFT)
            ttk.Entry(row, textvariable=self.dir_vars[i], width=80).pack(side=tk.LEFT, padx=5)
            ttk.Button(row, text="浏览...", 
                       command=lambda idx=i: self.select_directory(idx)).pack(side=tk.LEFT)

        # ----- 文件导航栏 -----
        nav_frame = ttk.Frame(self.root)
        nav_frame.pack(fill=tk.X, padx=10, pady=5)

        ttk.Button(nav_frame, text="← 上一个", command=self.prev_file).pack(side=tk.LEFT, padx=5)
        ttk.Button(nav_frame, text="下一个 →", command=self.next_file).pack(side=tk.LEFT, padx=5)
        ttk.Button(nav_frame, text="💾 保存全部三个文件", command=self.save_all).pack(side=tk.RIGHT, padx=5)

        self.file_label = ttk.Label(nav_frame, text="当前文件: ", font=('TkDefaultFont', 10, 'bold'))
        self.file_label.pack(side=tk.LEFT, padx=20)

        # ----- 三个文本编辑框（并排）-----
        edit_frame = ttk.Frame(self.root)
        edit_frame.pack(fill=tk.BOTH, expand=True, padx=10, pady=5)

        self.text_widgets = []
        self.edit_labels = []
        for i in range(3):
            col_frame = ttk.Frame(edit_frame)
            col_frame.pack(side=tk.LEFT, fill=tk.BOTH, expand=True, padx=3)

            lbl = ttk.Label(col_frame, text=f"目录 {i+1}", font=('TkDefaultFont', 9, 'bold'))
            lbl.pack(anchor=tk.W)
            self.edit_labels.append(lbl)

            # 使用ScrolledText方便编辑
            text = scrolledtext.ScrolledText(col_frame, wrap=tk.WORD, undo=True, font=('Consolas', 10))
            text.pack(fill=tk.BOTH, expand=True)
            self.text_widgets.append(text)

        # 状态栏
        self.status = ttk.Label(self.root, text="请先选择三个目录", relief=tk.SUNKEN, anchor=tk.W)
        self.status.pack(fill=tk.X, side=tk.BOTTOM)

        # 目录变更时自动重新扫描
        for var in self.dir_vars:
            var.trace_add('write', lambda *_: self.refresh_file_list())

    def select_directory(self, idx):
        """弹出目录选择对话框"""
        path = filedialog.askdirectory(title=f"选择目录 {idx+1}")
        if path:
            self.dir_vars[idx].set(path)

    def refresh_file_list(self):
        """根据当前三个目录，重新计算同名JSON文件列表"""
        paths = [var.get().strip() for var in self.dir_vars]
        # 如果有任何一个目录为空，则不清空列表，等待全部就绪
        if not all(paths):
            return

        try:
            # 只取三个目录下直接包含的 .json 文件（不递归子目录）
            sets = []
            for p in paths:
                files = {f for f in os.listdir(p) if f.lower().endswith('.json')}
                sets.append(files)

            # 三个目录的交集：同名文件
            common = sorted(sets[0] & sets[1] & sets[2])
            self.common_files = common
            self.current_index = 0 if common else -1

            if common:
                self.load_current_file()
                self.status.config(text=f"已加载 {len(common)} 个同名 JSON 文件")
            else:
                # 清空编辑框
                for text in self.text_widgets:
                    text.delete('1.0', tk.END)
                self.file_label.config(text="当前文件: 无")
                self.status.config(text="三个目录下没有同名的 JSON 文件")
        except Exception as e:
            messagebox.showerror("错误", f"读取目录失败:\n{e}")

    def load_current_file(self):
        """加载当前索引对应的三个文件到编辑框"""
        if self.current_index < 0 or not self.common_files:
            return

        filename = self.common_files[self.current_index]
        self.file_label.config(text=f"当前文件: {filename}")

        paths = [os.path.join(var.get(), filename) for var in self.dir_vars]

        for i, path in enumerate(paths):
            text_widget = self.text_widgets[i]
            try:
                with open(path, 'r', encoding='utf-8') as f:
                    raw = f.read()
                # 尝试美化 JSON 显示（保留原始数据，仅调整缩进）
                try:
                    parsed = json.loads(raw)
                    pretty = json.dumps(parsed, ensure_ascii=False, indent=2)
                except json.JSONDecodeError:
                    pretty = raw  # 不是有效JSON，直接显示原始文本
            except Exception as e:
                pretty = f"[读取失败] {e}"

            text_widget.delete('1.0', tk.END)
            text_widget.insert('1.0', pretty)

        # 更新目录标签为真实目录名
        for i, var in enumerate(self.dir_vars):
            dir_name = os.path.basename(var.get()) or f"目录{i+1}"
            self.edit_labels[i].config(text=dir_name)

        self.status.config(text=f"正在编辑: {filename}")

    def save_all(self):
        """保存三个编辑框中的内容到对应文件"""
        if self.current_index < 0 or not self.common_files:
            messagebox.showwarning("提示", "没有打开的文件可供保存")
            return

        filename = self.common_files[self.current_index]
        paths = [os.path.join(var.get(), filename) for var in self.dir_vars]

        if not messagebox.askyesno("确认保存", f"确定要覆盖保存以下三个文件吗？\n\n{chr(10).join(paths)}"):
            return

        success = True
        for i, path in enumerate(paths):
            content = self.text_widgets[i].get('1.0', 'end-1c')  # 去掉末尾自动添加的换行
            try:
                with open(path, 'w', encoding='utf-8') as f:
                    f.write(content)
            except Exception as e:
                messagebox.showerror("保存失败", f"无法写入文件:\n{path}\n错误: {e}")
                success = False

        if success:
            self.status.config(text=f"已保存: {filename}")
        else:
            self.status.config(text="保存过程出现错误")

    def prev_file(self):
        """切换到上一个文件"""
        if not self.common_files:
            return
        self.current_index = (self.current_index - 1) % len(self.common_files)
        self.load_current_file()

    def next_file(self):
        """切换到下一个文件"""
        if not self.common_files:
            return
        self.current_index = (self.current_index + 1) % len(self.common_files)
        self.load_current_file()

if __name__ == "__main__":
    root = tk.Tk()
    app = LocalizationEditor(root)
    root.mainloop()