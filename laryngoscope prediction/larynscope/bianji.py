import tkinter as tk
import cv2
import win32api
import sys
from tkinter import filedialog
from PIL import Image, ImageTk, ImageDraw
class Draw:
    def __init__(self,image_path):
        #初始化参数
        self.drawing = False
        self.last_x, self.last_y = 0, 0
        self.line_coordinates = []
        # 获取屏幕尺寸
        self.screen_width = win32api.GetSystemMetrics(0)
        self.screen_height = win32api.GetSystemMetrics(1)
        # 加载图片
        self.image=Image.open(image_path)
        self.image_width, self.image_height = self.image.size
        # 初始化图片
        self.scaled_width = round((self.screen_width*self.screen_height // (self.image_width * self.image_height)*0.5) ** 0.5 * self.image_width)
        self.scaled_height = round((self.screen_width * self.screen_height // (self.image_width * self.image_height)*0.5) ** 0.5 * self.image_height)
        self.scaled_image=self.image.resize((self.scaled_width,self.scaled_height))
        #建立窗口
        self.root=tk.Tk()
        ############################################
        menubar = tk.Menu(self.root)
        editmenu = tk.Menu(menubar, tearoff=0)
        editmenu.add_command(label="勾画", command=self.bianji)
        editmenu.add_command(label="还原", command=self.huanyuan)
        editmenu.add_command(label="保存", command=self.baocun)

        menubar.add_cascade(label="编辑", menu=editmenu)

        self.root.config(menu=menubar)
        ############################################################
        self.root.geometry(f"{int(self.screen_width*0.85)}x{int(self.screen_height*0.85)}")
        #绘图
        # 建立画布显示图片
        # 创建 canvas，并在 canvas_frame 中居中显示
        self.canvas = tk.Canvas(self.root, width=self.scaled_image.width, height=self.scaled_image.height)
        self.tk_image = ImageTk.PhotoImage(self.scaled_image)
        self.canvas.create_image(0, 0, anchor=tk.NW, image=self.tk_image)
        self.canvas.pack(expand=True)
        # 使用 pack() 函数居中显示 canvas
        # 绑定事件
        self.canvas.bind("<MouseWheel>", self.suofang)
        self.canvas.bind("<ButtonPress-3>", self.lbutton_press)
        self.canvas.bind("<B3-Motion>", self.lmove_press)
        self.canvas.bind("<ButtonRelease-3>", self.lbutton_release)
        self.root.mainloop()
    def lbutton_press(self, event):
        self.lx = event.x
        self.ly = event.y

    def lmove_press(self, event):
        delta_x = event.x - self.lx
        delta_y = event.y - self.ly
        new_x = self.canvas.winfo_x() + delta_x
        new_y = self.canvas.winfo_y() + delta_y
        self.canvas.place(x=new_x, y=new_y)
        self.lx = event.x
        self.ly = event.y

    def lbutton_release(self, event):
        pass
    def suofang(self,event):
        # 取消编辑
        self.canvas.unbind("<ButtonPress-1>")
        self.canvas.unbind("<ButtonRelease-1>")
        self.canvas.unbind("<B1-Motion>")
        self.line_coordinates = []
        delta = 1 if event.delta > 0 else -1
        if(delta>0):
            self.scaled_width = int(self.scaled_width * 1.1)
            self.scaled_height = int(self.scaled_height * 1.1)
        else:
            self.scaled_width = int(self.scaled_width * 0.9)
            self.scaled_height = int(self.scaled_height * 0.9)
        # 缩放图片并更新显示
        self.scaled_image = self.image.resize((self.scaled_width, self.scaled_height))
        self.tk_image = ImageTk.PhotoImage(self.scaled_image)
        self.canvas.config(width=self.scaled_width, height=self.scaled_height)
        self.canvas.create_image(0, 0, anchor=tk.NW, image=self.tk_image)
        self.canvas.update()
    def huanyuan(self):
        # 初始化图片
        self.canvas.delete("all")

        self.line_coordinates = []
        self.scaled_width = round((self.screen_width * self.screen_height // (self.image_width * self.image_height) *0.5) ** 0.5 * self.image_width)
        self.scaled_height = round((self.screen_width * self.screen_height // (self.image_width * self.image_height) *0.5) ** 0.5 * self.image_height)
        self.scaled_image = self.image.resize((self.scaled_width, self.scaled_height))
        self.tk_image = ImageTk.PhotoImage(self.scaled_image)
        self.canvas.config(width=self.scaled_width, height=self.scaled_height)
        self.canvas.create_image(0, 0, anchor=tk.NW, image=self.tk_image)
        self.canvas.pack(expand=True)
        self.canvas.update()
        #取消编辑
        self.canvas.unbind("<ButtonPress-1>")
        self.canvas.unbind("<ButtonRelease-1>")
        self.canvas.unbind("<B1-Motion>")
    def bianji(self):
        self.drawing = False
        self.last_x, self.last_y = 0, 0

        def on_button_press(event):
            self.drawing = True
            self.last_x, self.last_y = event.x, event.y
            #self.line_coordinates.append((event.x, event.y))
        def on_button_release(event):
            self.drawing = False
        def on_mouse_move(event):
            if self.drawing:
                draw = ImageDraw.Draw(self.scaled_image)
                draw.line((self.last_x, self.last_y, event.x, event.y), fill="red", width=2)
                self.tk_image = ImageTk.PhotoImage(self.scaled_image)
                self.canvas.create_image(0, 0, anchor=tk.NW, image=self.tk_image)
                self.line_coordinates.append((self.last_x, self.last_y, event.x, event.y))
                self.last_x, self.last_y = event.x, event.y
        self.canvas.bind("<ButtonPress-1>", on_button_press)
        self.canvas.bind("<ButtonRelease-1>", on_button_release)
        self.canvas.bind("<B1-Motion>", on_mouse_move)
    def baocun(self):
        file_path = filedialog.asksaveasfilename(initialdir="/", title="保存图像文件",
                                                 defaultextension=".jpg",
                                                 filetypes=(("JPEG 图像", "*.jpg"), ("PNG 图像", "*.png")))
        if file_path:
            new_image = Image.open(image_path)  # 复制原始图片
            draw = ImageDraw.Draw(new_image)  # 创建绘图对象
            if self.line_coordinates:
                for i in range(len(self.line_coordinates) - 1):
                    x0, y0,x1,y1= self.line_coordinates[i]
                    scaled_x0 = round(x0 * self.image_width / self.scaled_width)
                    scaled_y0 = round(y0 * self.image_height / self.scaled_height)
                    scaled_x1 = round(x1 * self.image_width / self.scaled_width)
                    scaled_y1 = round(y1 * self.image_height / self.scaled_height)
                    draw.line((scaled_x0, scaled_y0, scaled_x1, scaled_y1), fill="red", width=2)

            new_image.save(file_path)  # 保存路径
            self.line_coordinates = []
            self.huanyuan()