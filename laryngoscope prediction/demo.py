import os
import sys
from PyQt6.QtGui import QImage
from PyQt6.QtWidgets import QApplication, QMainWindow, QFileDialog
from PyQt6 import uic
import datetime
import shutil
from PyQt6.QtCore import QSize, Qt, QTimer, QSettings, pyqtSlot, pyqtSignal, QFile
from PyQt6.QtGui import QIcon, QPixmap, QGuiApplication, QIntValidator, QColor, QBrush, QMouseEvent
from PyQt6.QtWidgets import (QApplication, QMainWindow, QTableWidgetItem,
                             QAbstractItemView, QMessageBox, QWidget, QListView, QListWidgetItem, QLineEdit,
                             QHeaderView, QLabel, QHBoxLayout)
from PyQt6.QtCore import QThread, pyqtSignal
import time
from pymysql import Connection
import pygame#音频播放
from PyQt6.QtMultimedia import QMediaPlayer, QAudioOutput
from PyQt6.QtCore import QTimer, QUrl, Qt
from PyQt6.QtGui import QFont
import cv2
import json

from datetime import datetime
#图像算法
import larynscope.bianji as bianji
import larynscope.show_single_heatmap as show_single_heatmap
import larynscope.pic as pic

#嗓音算法



###UI###
from UI.InterfaceUi import *
from UI.LoginUi import *
from UI.predictSubWin import *
from UI.CheckRecord import *
from UI.predictVoice import Ui_predictSubVoice
from UI.predictVideo import Ui_predictSubVideo
from UI.modify_and_insert import *
from UI.add_case import *
from 声带疾病检测系统.demo.UI.voice.inferenceVoice import PredictVoice as deepPredictVoice

global modify_or_add
global doctor_id
modify_or_add = 0  # 判断是修改还是增加,为1的时候修改，为2的时候增加
global modify_id  # 在修改操作时，通过record_id在record表获得身份证号，用身份证号在patient表找到需要修改的病人
modify_id = ""
global insert_record_doctor_id  # 进行insert操作时，对record表进行添加，获得的医生id
insert_record_doctor_id = ""
global forget_or_register
forget_or_register = 0  # 判断执行忘记密码还是注册
global doctor_name
doctor_name="" #将医生姓名全局

host_ = 'localhost'
username_ = 'root'
password_ = 'root'

# 登录界面
class LoginWindow(QMainWindow):
    def __init__(self):
        super().__init__()
        self.mainWindow = None  # 主窗口类变量,如此设置是为了login函数结束后不被被垃圾回收
        self.ui = Ui_LoginWindow()
        self.ui.setupUi(self)

        self.ui.lineEdit_User.setFocus()

        self.setWindowFlag(QtCore.Qt.WindowType.FramelessWindowHint)  # 取消标题栏
        self.dragging = False  # 设置拖拽属性默认关闭
        self.offset = None  # 设置偏移属性
        self.setAttribute(QtCore.Qt.WidgetAttribute.WA_TranslucentBackground, True)
        # 创建阴影效果对象
        self.shadow = QtWidgets.QGraphicsDropShadowEffect(self)
        # 设置阴影效果属性
        self.shadow.setOffset(0, 0)  # 偏移量为5像素
        self.shadow.setBlurRadius(10)  # 模糊半径为10像素
        self.shadow.setColor(QtGui.QColor(0, 0, 0, 100))  # 阴影颜色为黑色，透明度为100
        # 将阴影效果应用于窗口
        self.setGraphicsEffect(self.shadow)
        # 显示提示信息的定时器
        self.timer = QTimer(self)  # 登录页面的定时器作为类的成员变量
        self.timer1 = QTimer(self)  # 注册页面的定时器
        self.timer.timeout.connect(self.clearErrorMessage)  # 初始连接登录页面槽函数
        self.timer1.timeout.connect(self.clearErrorMessage1)  # 初始连接注册页面错误信息槽函数
        # 绑定登录事件
        self.ui.pushButton_2.clicked.connect(self.login)
        # 绑定跳转到注册页面
        self.ui.pushButton_3.clicked.connect(self.goto_register)
        # 绑定跳转到注册页面
        self.ui.pushButton_4.clicked.connect(self.goto_register_forget)
        # 绑定注册按钮
        self.ui.pushButton_ok.clicked.connect(self.register)
        # 绑定注册页面中的返回按钮
        self.ui.pushButton_back.clicked.connect(self.register_back)
        # 绑定登录页面显示密码按钮
        self.ui.checkBox_show.stateChanged.connect(self.ShowPassword)
        # 绑定注册页面显示密码按钮
        self.ui.checkBox_show_2.stateChanged.connect(self.ShowPassword_2)
        # 绑定缩小按钮
        self.ui.pushButton_suoxiao.clicked.connect(self.suoxiao)
        self.ui.pushButton_suoxiao_2.clicked.connect(self.suoxiao)

        #设置回车键事件
        # 连接信号：回车键按下时切换焦点或触发登录函数
        self.ui.lineEdit_User.returnPressed.connect(self.focus_on_password)
        self.ui.lineEdit_Psw.returnPressed.connect(self.login)

        #设置tab键顺序
        self.setTabOrder(self.ui.lineEdit_User, self.ui.lineEdit_Psw)
        self.setTabOrder(self.ui.lineEdit_Psw, self.ui.pushButton_2)

        self.show()

    def focus_on_password(self):
        # 当用户名输入完成后，设置焦点到密码输入框
        self.ui.lineEdit_Psw.setFocus()

    # 用户登录判断逻辑,此处逻辑仅作测试用,实际需依据数据库更改
    def login(self):
        username = self.ui.lineEdit_User.text()
        password = self.ui.lineEdit_Psw.text()
        global doctor_id
        doctor_id = check_password(username, password)
        if len(doctor_id) == 1:
            # 打开主窗口
            global doctor_name
            global insert_record_doctor_id
            doctor_name = self.ui.lineEdit_User.text()
            insert_record_doctor_id = str(doctor_id)
            self.mainWindow = MainWindow(doctor_id[0][0])
            # 关闭当前窗口
            self.deleteLater()
        else:
            self.ui.label_tishi.setText('用户名或密码错误')
            self.timer.stop()  # 停止定时器（如果它正在运行）
            self.timer.start(3000)  # 重新设置定时器并启动

    # 最小化窗口
    def suoxiao(self):
        self.showMinimized()

    # 重写鼠标按下事件
    def mousePressEvent(self, event):
        if event.button() == Qt.MouseButton.LeftButton and self.geometry().contains(self.mapToGlobal(event.pos())):
            self.dis = self.mapToGlobal(event.pos()) - self.pos()
            # print(self.mapToGlobal(event.pos()),event.pos(),self.pos(),self.dis)
            # print("鼠标左键在指定区域内被按下")
            self.dragging = True
            self.setCursor(Qt.CursorShape.ClosedHandCursor)

    # 重写鼠标移动事件
    def mouseMoveEvent(self, event):
        if self.dragging:
            self.move(self.mapToGlobal(event.pos()) - self.dis)

    # 释放鼠标事件
    def mouseReleaseEvent(self, event):
        if event.button() == Qt.MouseButton.LeftButton and self.dragging:
            self.dragging = False
            self.setCursor(Qt.CursorShape.OpenHandCursor)

    def clearErrorMessage(self):
        self.ui.label_tishi.clear()

    def clearErrorMessage1(self):
        self.ui.label_register_tishi.clear()

    def goto_register(self):
        global forget_or_register
        forget_or_register = 1  # 等于1时执行注册操作
        self.ui.stackedWidget.setCurrentIndex(1)
        self.ui.lineEdit_Psw_resgier.clear()
        self.ui.lineEdit_User_resgier.clear()
        self.ui.lineEdit_RePsw_resgier.clear()
        self.ui.lineEdit_Psw.clear()
        self.ui.lineEdit_User.clear()

    def goto_register_forget(self):
        global forget_or_register
        forget_or_register = 2  # 等于2时执行忘记密码操作
        self.ui.stackedWidget.setCurrentIndex(1)
        self.ui.lineEdit_Psw_resgier.clear()
        self.ui.lineEdit_User_resgier.clear()
        self.ui.lineEdit_RePsw_resgier.clear()
        self.ui.lineEdit_Psw.clear()
        self.ui.lineEdit_User.clear()

    # 注册的前端数据读取(!!!!!!!!!需要将数据读入到数据库中并进行一系列判断逻辑)
    def register(self):
        # 医生编号
        username = self.ui.lineEdit_User_resgier.text()
        # 密码
        password = self.ui.lineEdit_Psw_resgier.text()
        # 确认密码
        repassword = self.ui.lineEdit_RePsw_resgier.text()
        # 检查用户名、密码和重复密码是否都不为空
        if username != "" and password != "" and repassword != "":
            # 检查密码和重复密码是否匹配
            if password == repassword:
                con = Connection(
                    host= host_ ,
                    user= username_ ,
                    password= password_ ,
                )
                cur = con.cursor()
                sql = """select * from lary.doctors where username=%s"""
                cur.execute(sql, username)
                res = cur.fetchone()
                global forget_or_register
                if forget_or_register == 1:
                    if res is None:
                        # 显示注册成功的弹窗
                        sql = ("insert into lary.doctors(username,password) values(%s,%s)")
                        cur.execute(sql, (username, password))
                        con.commit()
                        con.close()
                        msgBox = QMessageBox(self)
                        msgBox.setWindowTitle('注册成功')
                        msgBox.setText('您的注册已成功完成！')
                        msgBox.setIcon(QMessageBox.Icon.Information)
                        msgBox.setStandardButtons(QMessageBox.StandardButton.Ok)
                        self.ui.lineEdit_Psw_resgier.clear()
                        self.ui.lineEdit_User_resgier.clear()
                        self.ui.lineEdit_RePsw_resgier.clear()
                        msgBox.show()
                    else:
                        self.ui.label_register_tishi.setText('该用户已存在!')
                        self.timer1.stop()  # 如果定时器正在运行，停止它
                        self.timer1.start(3000)  # 重新设置定时器时间并启动它
                        con.close()
                elif forget_or_register == 2:
                    if res is None:
                        self.ui.label_register_tishi.setText('该用户不存在!')
                        self.timer1.stop()  # 如果定时器正在运行，停止它
                        self.timer1.start(3000)  # 重新设置定时器时间并启动它
                        con.close()
                    else:
                        # 显示注册成功的弹窗
                        sql = ("update lary.doctors set password=%s where username=%s")
                        cur.execute(sql, (password, username))
                        con.commit()
                        con.close()
                        msgBox = QMessageBox(self)
                        msgBox.setWindowTitle('修改密码成功')
                        msgBox.setText('您的密码已修改完成！')
                        msgBox.setIcon(QMessageBox.Icon.Information)
                        msgBox.setStandardButtons(QMessageBox.StandardButton.Ok)
                        msgBox.show()
            else:
                # 密码和重复密码不匹配
                self.ui.label_register_tishi.setText('密码和重复密码不一致!')
                self.timer1.stop()  # 如果定时器正在运行，停止它
                self.timer1.start(3000)  # 重新设置定时器时间并启动它
        else:
            # 用户名、密码或重复密码为空
            self.ui.label_register_tishi.setText('请输入所有必填项!')
            self.timer1.stop()  # 如果定时器正在运行，停止它
            self.timer1.start(3000)  # 重新设置定时器时间并启动它

            # 显示密码

    def ShowPassword(self):
        if self.ui.checkBox_show.isChecked():
            self.ui.lineEdit_Psw.setEchoMode(QLineEdit.EchoMode.Normal)
        else:
            self.ui.lineEdit_Psw.setEchoMode(QLineEdit.EchoMode.Password)

    def ShowPassword_2(self):
        if self.ui.checkBox_show_2.isChecked():
            self.ui.lineEdit_Psw_resgier.setEchoMode(QLineEdit.EchoMode.Normal)
            self.ui.lineEdit_RePsw_resgier.setEchoMode(QLineEdit.EchoMode.Normal)
        else:
            self.ui.lineEdit_Psw_resgier.setEchoMode(QLineEdit.EchoMode.Password)
            self.ui.lineEdit_RePsw_resgier.setEchoMode(QLineEdit.EchoMode.Password)

    def register_back(self):
        self.ui.stackedWidget.setCurrentIndex(0)


# 主界面
class MainWindow(QMainWindow):
    def __init__(self, doctor_id):
        super().__init__()
        self.ui = Ui_MainWindow()
        self.ui.setupUi(self)

        self.subWidgetModOrInsert = None  # 修改或添加子界面
        self.loginWin = None  # 登陆界面
        global doctor_name
        self.ui.label_doctorName.setText('登录用户：'+doctor_name)
        result, self.row, self.col = read_user(doctor_id)  # 数据库表的行列

        # 病人管理->设置行和列
        self.ui.tableWidget.setRowCount(self.row)
        self.ui.tableWidget.setColumnCount(self.col + 1)
        (self.ui.tableWidget.  # 就诊记录ID项:方便后续查询
         setHorizontalHeaderLabels(['就诊记录ID', '就诊时间', '姓名', '年龄', '性别', '操作']))
        # 填充内容
        for i in range(self.row):
            for j in range(self.col):
                data = QTableWidgetItem(str(result[i][j]))
                if j == 2:
                    data.setForeground(QBrush(QColor('blue')))
                data.setTextAlignment(Qt.AlignmentFlag.AlignCenter)  # 设置文本居中
                self.ui.tableWidget.setItem(i, j, data)

        self.ui.tableWidget.setEditTriggers(QAbstractItemView.EditTrigger.NoEditTriggers)
        # 连接tablewidget表格 信号到槽函数
        self.ui.tableWidget.cellClicked.connect(self.onCellClicked)

        # 最后一列填充三个button控件
        for i in range(self.row):
            buttons = self.pat_buttons()
            self.ui.tableWidget.setCellWidget(i, self.col, buttons)

        self.ui.tableWidget.verticalHeader().setVisible(False)  # 隐藏第一列序号
        self.ui.tableWidget.setEditTriggers(QAbstractItemView.EditTrigger.NoEditTriggers)  # 禁止编辑单元格
        self.ui.tableWidget.setAlternatingRowColors(True)  # 使表格颜色交错显示

        # 设置控件功能
        self.ui.pushButton_2.clicked.connect(self.back_login)
        self.ui.pushButton_3.clicked.connect(self.insert)  # 添加病人
        self.ui.pushButton_4.clicked.connect(self.search)  # 查找病人
        self.ui.pushButton_fresh.clicked.connect(self.refresh)
        self.center()
        self.show()

    def onCellClicked(self, row, column):
        self.data = [''] * 4
        if column == 2:
            # 获取被点击的单元格的就诊时间
            for i in range(1, 5):
                self.data[i - 1] = self.ui.tableWidget.item(row, i).text()
            # 弹出查询记录的窗口
            self.check = CheckRecord(self.data)

    def center(self):
        qr = self.frameGeometry()
        # 获取主屏幕的可用几何中心
        cp = QGuiApplication.primaryScreen().availableGeometry().center()
        qr.moveCenter(cp)
        self.move(qr.topLeft())

    def back_main_win(self):
        self.ui.stackedWidget.setCurrentIndex(1)
        self.refresh(doctor_id)


    def back_login(self):
        self.loginWin = LoginWindow()
        self.deleteLater()

    # 设置每列宽度
    def resizeEvent(self, event):
        table_width = self.ui.frame_6.width()
        weight = [1, 2, 1, 1, 1, 1]
        for i in range(self.col):
            self.ui.tableWidget.setColumnWidth(i, int(table_width * weight[i] / sum(weight)))
        self.ui.tableWidget.horizontalHeader().setStretchLastSection(True)  # 设置最后一列自动填充

    # 病人列表右侧的操作
    def pat_buttons(self):
        widget = QtWidgets.QWidget()
        # 定义button
        predBtn = QtWidgets.QPushButton('喉镜预测')
        predBtn.clicked.connect(self.pred_win)
        #音频预测界面
        predVoiceBtn = QtWidgets.QPushButton('音频预测')
        predVoiceBtn.clicked.connect(self.pred_voice_win)
        #视频预测界面
        predVideoBtn = QtWidgets.QPushButton('视频预测')
        predVideoBtn.clicked.connect(self.pred_video_win)

        updateBtn = QtWidgets.QPushButton('修改')
        updateBtn.clicked.connect(self.modify)
        deleteBtn = QtWidgets.QPushButton('删除')
        deleteBtn.clicked.connect(self.delete)

        hLayout = QtWidgets.QHBoxLayout()
        hLayout.addWidget(predBtn)
        hLayout.addWidget(predVoiceBtn)
        hLayout.addWidget(predVideoBtn)
        hLayout.addWidget(updateBtn)
        hLayout.addWidget(deleteBtn)
        hLayout.setContentsMargins(0, 0, 0, 0)
        hLayout.setSpacing(0)
        widget.setLayout(hLayout)
        return widget

    # 获取预测,修改,删除所在行
    def buttons_row(self):
        button = self.sender()
        if button:
            return self.ui.tableWidget.indexAt(button.parent().pos()).row()
        else:
            return -1

    # 修改病人信息,调用子界面Ui_modify_and_insert
    def modify(self):
        global modify_or_add
        modify_or_add = 1
        self.subWidgetModOrInsert = ModOrInsertWindow()
        self.subWidgetModOrInsert.ui.pushButton.clicked.connect(self.modify_or_add_refresh)
        self.subWidgetModOrInsert.ui.mod_or_ins.setText('修改病人信息')
        print("self.subWidgetModOrInsert.ui.mod_or_ins.setText('修改病人信息')")
        # 获取第一列self.subWidgetModOrInsert.ui.lineEdit_1.setText(self.ui.tableWidget.item(self.buttons_row(),0).text())
        global modify_id
        modify_id = select_patientid_by_recordid(self.ui.tableWidget.item(self.buttons_row(), 0).text())
        modify_result = select_patient(modify_id)
        self.subWidgetModOrInsert.ui.lineEdit_1.setReadOnly(True)  # 由于身份证号在patient中作为主码，所以这里设置成只读
        self.subWidgetModOrInsert.ui.lineEdit_1.setText(str(modify_result[0][0]))
        self.subWidgetModOrInsert.ui.lineEdit_2.setText(str(modify_result[0][1]))
        self.subWidgetModOrInsert.ui.lineEdit_3.setText(str(modify_result[0][2]))
        self.subWidgetModOrInsert.ui.lineEdit_4.setText(str(modify_result[0][3]))
        self.subWidgetModOrInsert.ui.lineEdit_5.setText(str(modify_result[0][4]))
        self.subWidgetModOrInsert.ui.lineEdit_6.setText(str(modify_result[0][5]))
        if (str(modify_result[0][6]) == "男"):
            self.subWidgetModOrInsert.ui.radioButton_man.setChecked(True)
        elif (str(modify_result[0][6]) == "女"):
            self.subWidgetModOrInsert.ui.radioButton_woman.setChecked(True)

    def search(self):
        select_index = self.ui.comboBox.currentIndex()  # 0表示请查询 1表示姓名 2表示id 用%实现模糊搜索
        tab = self.ui.lineEdit.text()
        if (select_index == 0 or tab == ""):
            global insert_record_doctor_id
            self.refresh(str(insert_record_doctor_id[2][0]))
        elif (select_index == 1):  # 用姓名查找
            self.refreshbyname(str(self.ui.lineEdit.text()))
        elif (select_index == 2):  # 用id查找
            self.refreshbyid(str(self.ui.lineEdit.text()))

    def insert(self):
        #添加诊断记录  两种方式
        #选择已有的病人为复诊
        #输入新的病人信息
        global modify_or_add
        modify_or_add = 2
        self.addcase=AddCase()
        self.addcase.hasAddCase.connect(self.refresh)
        self.addcase.show()


    def handle_id_submitted(self,id):
        self.refresh(id)

    def modify_or_add_refresh(self):
        global insert_record_doctor_id
        self.refresh(str(insert_record_doctor_id[2][0]))

    def delete(self):
        record_id=self.ui.tableWidget.item(self.buttons_row(), 0).text()
        choice = QMessageBox.warning(self, '警告', '确定要删除该病人吗?',
                                     QMessageBox.StandardButton.Yes | QMessageBox.StandardButton.No,
                                     QMessageBox.StandardButton.No)
        if choice == QMessageBox.StandardButton.Yes:
            con = Connection(
                host=host_,
                user=username_,
                password=password_,
            )
            with con.cursor() as cur:
                sql = """delete from lary.record where record_id = %s"""
                # 要查找的新值
                cur.execute(sql, record_id)
                con.commit()
            con.close()
        global insert_record_doctor_id
        self.refresh(str(insert_record_doctor_id[2][0]))

    def pred_video_win(self):
        time = self.ui.tableWidget.item(self.buttons_row(), 1).text()
        name = self.ui.tableWidget.item(self.buttons_row(), 2).text()
        age = self.ui.tableWidget.item(self.buttons_row(), 3).text()
        id = self.ui.tableWidget.item(self.buttons_row(), 0).text()

        data = [time, name, age, id]

        self.pageVideo=preidctVideo()
        self.pageVideo.showPatInfo(data)
        self.pageVideo.show()


    # 音频预测被按下
    def pred_voice_win(self):
        time = self.ui.tableWidget.item(self.buttons_row(), 1).text()
        name = self.ui.tableWidget.item(self.buttons_row(), 2).text()
        age = self.ui.tableWidget.item(self.buttons_row(), 3).text()
        id = self.ui.tableWidget.item(self.buttons_row(), 0).text()

        data = [time, name, age, id]

        self.pageVoice=preidctVoice()
        self.pageVoice.showPatInfo(data)
        self.pageVoice.show()

    # 预测被按下
    def pred_win(self):

        time = self.ui.tableWidget.item(self.buttons_row(), 1).text()
        name = self.ui.tableWidget.item(self.buttons_row(), 2).text()
        age = self.ui.tableWidget.item(self.buttons_row(), 3).text()
        record_id = self.ui.tableWidget.item(self.buttons_row(), 0).text()
        data = [time, name, age, record_id]

        self.pagePic=picture_side(data)
        #self.pagePic.showPatInfo(data)
        self.pagePic.show()


    def refresh(self, doctor_id):
        print("def refresh(self, doctor_id):")
        result, self.row, self.col = read_user(doctor_id)  # 数据库表的行列
        # 病人管理->设置行和列
        self.ui.tableWidget.setRowCount(self.row)
        self.ui.tableWidget.setColumnCount(self.col + 1)
        (self.ui.tableWidget.  # 就诊记录ID项:方便后续查询
         setHorizontalHeaderLabels(['就诊记录ID', '就诊时间', '姓名', '年龄', '性别', '操作']))
        # 填充内容
        for i in range(self.row):
            for j in range(self.col):
                data = QTableWidgetItem(str(result[i][j]))
                if j == 2:
                    data.setForeground(QBrush(QColor('blue')))
                data.setTextAlignment(Qt.AlignmentFlag.AlignCenter)  # 设置文本居中
                self.ui.tableWidget.setItem(i, j, data)

        # 最后一列填充三个button控件
        for i in range(self.row):
            buttons = self.pat_buttons()
            self.ui.tableWidget.setCellWidget(i, self.col, buttons)

        self.ui.tableWidget.verticalHeader().setVisible(False)  # 隐藏第一列序号
        self.ui.tableWidget.setEditTriggers(QAbstractItemView.EditTrigger.NoEditTriggers)  # 禁止编辑单元格
        self.ui.tableWidget.setAlternatingRowColors(True)  # 使表格颜色交错显示

    def refreshbyname(self, patient_name):
        result, self.row, self.col = read_user_by_name(patient_name)  # 数据库表的行列
        if not result:
            self.ui.tableWidget.setColumnCount(6)
        else:
            # 病人管理->设置列
            self.ui.tableWidget.setColumnCount(self.col + 1)
        # 病人管理->设置行
        self.ui.tableWidget.setRowCount(self.row)
        (self.ui.tableWidget.  # 就诊记录ID项:方便后续查询
         setHorizontalHeaderLabels(['就诊记录ID', '就诊时间', '姓名', '年龄', '性别', '操作']))
        # 填充内容
        for i in range(self.row):
            for j in range(self.col):
                data = QTableWidgetItem(str(result[i][j]))
                if j == 2:
                    data.setForeground(QBrush(QColor('blue')))
                data.setTextAlignment(Qt.AlignmentFlag.AlignCenter)  # 设置文本居中
                self.ui.tableWidget.setItem(i, j, data)

        # 最后一列填充三个button控件
        for i in range(self.row):
            buttons = self.pat_buttons()
            self.ui.tableWidget.setCellWidget(i, self.col, buttons)

        self.ui.tableWidget.verticalHeader().setVisible(False)  # 隐藏第一列序号
        self.ui.tableWidget.setEditTriggers(QAbstractItemView.EditTrigger.NoEditTriggers)  # 禁止编辑单元格
        self.ui.tableWidget.setAlternatingRowColors(True)  # 使表格颜色交错显示

    def refreshbyid(self, patient_id):
        result, self.row, self.col = read_user_by_id(patient_id)  # 数据库表的行列
        if not result:
            self.ui.tableWidget.setColumnCount(6)
        else:
            # 病人管理->设置列
            self.ui.tableWidget.setColumnCount(self.col + 1)
            # 病人管理->设置行
        self.ui.tableWidget.setRowCount(self.row)
        (self.ui.tableWidget.  # 就诊记录ID项:方便后续查询
         setHorizontalHeaderLabels(['就诊记录ID', '就诊时间', '姓名', '年龄', '性别', '操作']))
        # 填充内容
        for i in range(self.row):
            for j in range(self.col):
                data = QTableWidgetItem(str(result[i][j]))
                if j == 2:
                    data.setForeground(QBrush(QColor('blue')))
                self.ui.tableWidget.setItem(i, j, data)

        # 最后一列填充三个button控件
        for i in range(self.row):
            buttons = self.pat_buttons()
            self.ui.tableWidget.setCellWidget(i, self.col, buttons)

        self.ui.tableWidget.verticalHeader().setVisible(False)  # 隐藏第一列序号
        self.ui.tableWidget.setEditTriggers(QAbstractItemView.EditTrigger.NoEditTriggers)  # 禁止编辑单元格
        self.ui.tableWidget.setAlternatingRowColors(True)  # 使表格颜色交错显示


# 自定义label触发事件,必须在predictSubWin.py中将对应label进行改写
class ClickableLabel(QLabel):
    clicked = pyqtSignal()

    # 重写鼠标事件用于label的点击信号

    def mousePressEvent(self, event: QMouseEvent):
        if event.button() == Qt.MouseButton.LeftButton:
            self.clicked.emit()

class PicWorkerThread(QThread):
    progress_changed = pyqtSignal(int)  # 用于更新进度条
    result_ready = pyqtSignal(str)  # 用于返回结果信息
    image_ready = pyqtSignal(str)  # 用于返回处理后的图片路径

    def __init__(self, img, img_name,parent=None):
        super().__init__(parent)
        self.img = img  # 要处理的图像路径
        self.imgName = img_name

    def run(self):
        self.progress_changed.emit(0)  # 假设白光预测占50%
        self.progress_changed.emit(20)  # 假设白光预测占50%
        # 模拟耗时的操作并更新进度
        # 1. 进行白光预测
        predict = pic.detect_soft_NBI(self.img)
        # print(predict)
        if predict == 0:
            name = '健康'
        elif predict == 1:
            name = '喉鳞状细胞癌'
        elif predict == 3:
            name = '炎症角化'
        else:
            name = '不典型增生'

        # 2. 更新进度条
        self.progress_changed.emit(50)  # 假设白光预测占50%

        # 3. 生成热图
        heatMap=show_single_heatmap.run(self.img, predict)
        print("生成热图")
        heatMapName=self.imgName.split("/")[-1]
        heatmap_savepath=os.path.join("store","heatmap")
        if not os.path.exists(heatmap_savepath):
            os.makedirs(heatmap_savepath)
        cv2.imwrite(os.path.join(heatmap_savepath,heatMapName), heatMap)

        # 4. 更新进度条
        self.progress_changed.emit(100)  # 热图生成完毕，占100%

        # 5. 发送结果和图片路径到主线程
        self.result_ready.emit(name)
        self.image_ready.emit(os.path.join(heatmap_savepath,heatMapName))

# 缩略图功能and预测窗口功能
class picture_side(QMainWindow):
    def __init__(self,data):
        super().__init__()
        self.pix = None  # 当前图片
        self.scaled_image = None
        self.ui = Ui_predictSubWin()
        self.ui.setupUi(self)

        self.record_id = data[3]
        text = "病历编号"+data[3] +"\n" +"病人:" + data[1] + "\n" + "年龄:" + data[2] + "\n" + "就诊时间:" + data[0]
        self.ui.label_2.setText(text)

        self.defaultImFolder = "D:/laryImg/class6_aug/train"  # 初始化defaultImFolder属性
        self.currentImgIdx = 0  # 缩略图中点击图片序号默认值为0

        self.imageNameList = []
        self.img_paths = []  # 存储缩略图的图片路径
        self.heatMapNameList=[]

        #创建时应该从数据库中读取已经保存的图像名称   并在窗口中显示
        self.initWindow()

        self.ui.listWidget.setFlow(QListView.Flow(1))
        self.ui.listWidget.setIconSize(QSize(50, 100))

        global doctor_name
        self.ui.label_doc_name.setText(doctor_name)
        self.ui.pushButton_openfile.clicked.connect(self.OpenPictureDir)
        self.ui.pushButton_openfolder.clicked.connect(self.OpenFolder)
        self.ui.pushButton.clicked.connect(self.Upload)
        self.ui.pushButton_white.clicked.connect(self.predictwhite)
        self.ui.pushButton_NBI.clicked.connect(self.predictnbi)
        # 缩略图的信号连接
        self.ui.listWidget.itemSelectionChanged.connect(self.loadImage)
        #self.record_id = data[3]
        # 点击图片放大编辑的信号连接
        #self.ui.label_oriImg.clicked.connect(self.edit)
        # 初始化进度条
        self.ui.progressBar.setValue(0)
        self.ui.progressBar.setVisible(False)

    def initWindow(self):
        con = Connection(
            host=host_,
            user=username_,
            password=password_,
            database="lary",
        )
        try:
            with con.cursor() as cur:
                sql = """SELECT result, img_name, imgtag_name, text ,heatmap
                         FROM record
                         WHERE record_id=%s"""
                # 修正参数为元组，添加逗号
                cur.execute(sql, (self.record_id,))

                # 获取查询结果
                result = cur.fetchone()

                if result:
                    # 解包并打印结果
                    res, img_name_List, imgtag_name, text,heatmap = result
                    if img_name_List is None:
                        img_name_List = []
                    else:
                        try:
                            img_name_List = json.loads(img_name_List)  # 反序列化为Python列表:ml-citation{ref="3,7" data="citationList"}
                        except json.JSONDecodeError:
                            return
                else:
                    print(f"未找到ID为 {self.record_id} 的记录")

        finally:
            con.close()

        self.ui.label_6.setText(f"<b>{imgtag_name}</b>")  # 加粗显示
        self.ui.plainTextEdit.setPlainText(text)  # 保留原始换行符
        index = self.ui.comboBox.findText(res)
        self.ui.comboBox.setCurrentIndex(index)

        self.imageNameList=img_name_List
        #将imgnameList进行拼接输入到self.img_paths
        for imageName in img_name_List:
            self.img_paths.append( os.path.join(os.getcwd(), 'store', 'larynscope',imageName))
        #对缩略图和大图进行初始化
        self._update_ui_after_selection(self.img_paths)
        self.curImgId = 0

    def edit(self):
        img = self.img_paths[self.curImgId]
        img = img.replace('\\', '/')
        run = bianji.Draw(img)

    def predictwhite(self):
        img = self.img_paths[self.curImgId]
        img = img.replace('\\', '/')
        self.ui.progressBar.setVisible(True)
        # 创建并启动线程
        self.worker = PicWorkerThread(img)
        self.worker.progress_changed.connect(self.update_progress)
        self.worker.result_ready.connect(self.update_result)
        self.worker.image_ready.connect(self.update_image)
        self.worker.start()

    def predictnbi(self):
        img = self.img_paths[self.curImgId]
        img_name=self.imageNameList[self.curImgId]

        img = img.replace('\\', '/')
        self.ui.progressBar.setVisible(True)
        # 创建并启动线程
        self.worker = PicWorkerThread(img,img_name)
        self.worker.progress_changed.connect(self.update_progress)
        self.worker.result_ready.connect(self.update_result)
        self.worker.image_ready.connect(self.update_image)
        self.worker.start()

    def update_progress(self, value):
        self.ui.progressBar.setValue(value)  # 更新进度条的值

    def update_result(self, name):
        self.ui.label_6.setText(f'{name}')  # 更新预测结果

    def update_image(self, image_path):
        self.ui.label_4.setPixmap(QPixmap(image_path))  # 更新显示的图片
        self.heatMapNameList.append(image_path.split("/")[-1]) #加入热力图队列
        self.ui.label_4.setScaledContents(True)
        self.ui.progressBar.setVisible(False)

    def Upload(self):
        con = Connection(
            host=host_,
            user=username_,
            password=password_,
        )
        try:
            with con.cursor() as cur:
                text = self.ui.plainTextEdit.toPlainText().strip()  # 获取并去除空白
                type = self.ui.comboBox.currentText()  # 获取当前选择值

                # 验证逻辑
                if type == "请选择类型" or not text:  # 假设默认选项是"请选择类型"
                    error_msgs = []
                    if type == "请选择类型":
                        error_msgs.append("请选择诊断类型")
                    if not text:
                        error_msgs.append("请输入批注内容")

                    # 弹窗提示（需导入 QMessageBox）
                    QMessageBox.warning(self, "输入不完整",
                                        "请完成以下操作：\n" + "\n".join(error_msgs))
                    return  # 终止操作

                # 执行数据库更新
                sql = """UPDATE lary.record
                        SET result=%s, text=%s
                        WHERE record_id=%s"""
                cur.execute(sql, (type, text, self.record_id))
                con.commit()
        finally:
            con.close()

        #上传图像路径
        # 将所选路径上传到数据库
        # MySQL 连接
        con = Connection(
            host=host_,
            user=username_,
            password=password_,
        )

        cursor = con.cursor()
        imgtag_name = self.ui.label_6.text()
        # 分支处理逻辑
        if not imgtag_name:  # 检测空值
            # 方案1: 中断操作并提示用户:ml-citation{ref="5,6" data="citationList"}
            cursor.execute("UPDATE lary.record SET img_name = %s WHERE record_id = %s", (json.dumps(self.imageNameList),self.record_id))
            return
        else:
            # 插入数据
            cursor.execute("UPDATE lary.record SET img_name = %s,imgtag_name=%s,heatmap=%s WHERE record_id = %s", (json.dumps(self.imageNameList),imgtag_name,json.dumps(self.heatMapNameList),self.record_id))

        # 提交并关闭
        con.commit()
        cursor.close()
        con.close()

        msgBox = QMessageBox(self)
        msgBox.setWindowTitle('提醒')
        msgBox.setText('上传成功！')
        msgBox.setIcon(QMessageBox.Icon.Information)
        msgBox.setStandardButtons(QMessageBox.StandardButton.Ok)
        msgBox.exec()  # 显示消息框并等待用户操作

    def back_main_win(self):
        global doctor_id
        self.mainwindow = MainWindow(doctor_id[0][0])
        self.deleteLater()
        self.parent().window().close()
        self.parent().window().deleteLater()  # 关闭原本窗口

    def OpenFolder(self):
        # 打开文件夹选择对话框
        folder = QFileDialog.getExistingDirectory(self, '选择图像所在路径', self.defaultImFolder)
        if folder:
            print(f'Selected Folder: {folder}')
            # 遍历文件夹，筛选出图像文件
            valid_image_extensions = ['.png', '.xpm', '.jpg', '.bmp', '.gif']
            for filename in os.listdir(folder):
                if any(filename.lower().endswith(ext) for ext in valid_image_extensions):
                    self.imageNameList.append(filename)
            self.defaultImFolder=folder
            self.imgFolder=folder
            self.add_image_items()  # 调用缩略图函数
            if self.imageNameList:
                # 获取第一张图片的路径
                imgPath = os.path.join(self.imgFolder, self.imageNameList[0])
                # 绘制图片，显示出来
                self.pix = QtGui.QPixmap(imgPath)
                self.scaleImage()
                # self.ui.label.setPixmap(self.pix)
                # 标记第一张图片的id为0
                self.curImgId = 0
                self.ui.listWidget.setCurrentRow(self.curImgId)
                print("list widget size",self.ui.listWidget.size())

    def OpenPictureDir(self):
        """打开图片选择对话框并处理选择的图片文件"""
        # 配置初始目录（保留上次选择的目录）
        initial_dir = self.default_img_folder if hasattr(self, 'default_img_folder') else QtCore.QDir.homePath()

        # 弹出文件选择对话框
        selected_files, _ = QtWidgets.QFileDialog.getOpenFileNames(
            None,
            "选择图像文件",
            initial_dir,
            "图像文件 (*.png *.jpg *.jpeg)"
        )

        # 处理用户取消操作
        if not selected_files:
            print("操作取消：未选择文件")
            return

        # 更新默认目录（使用跨平台路径处理）
        self.default_img_folder = os.path.dirname(selected_files[0])

        # 准备目标目录
        target_folder = os.path.join(os.getcwd(), 'store', 'larynscope')
        os.makedirs(target_folder, exist_ok=True)

        # 有效图片扩展名集合
        allowed_extensions = {'.png', '.jpg', '.jpeg'}
        new_img_paths = []
        new_file_names = []

        # 处理每个选择的文件
        for src_path in selected_files:
            # 验证文件扩展名
            file_ext = os.path.splitext(src_path)[1].lower()
            if file_ext not in allowed_extensions:
                continue

            # 生成目标路径
            filename = os.path.basename(src_path)
            dest_path = os.path.join(target_folder, filename)

            # 执行文件复制
            try:
                shutil.copy(src_path, dest_path)
                new_img_paths.append(dest_path)
                new_file_names.append(filename)
            except Exception as e:
                print(f"文件复制失败：{src_path} -> {dest_path}\n错误信息：{str(e)}")
                continue

        # 检查是否有有效文件
        if not new_img_paths:
            print("警告：未找到有效图片文件")
            return

        # 更新类数据成员
        self.img_paths += new_img_paths  # 保留历史记录（如需替换改为=）
        self.imageNameList += new_file_names

        # 更新界面显示
        self._update_ui_after_selection(new_img_paths)

    def _update_ui_after_selection(self, new_img_paths):
        """处理选择文件后的UI更新"""
        # 添加缩略图到列表
        self.add_image_items()

        # 显示首张图片
        if new_img_paths:
            self.current_img_id = len(self.img_paths) - len(new_img_paths)  # 定位到新添加的第一张
            self._display_image(new_img_paths[0])
            self.ui.listWidget.setCurrentRow(self.current_img_id)

    def _display_image(self, image_path):
        image_name = os.path.basename(image_path)
        heatmap_path = os.path.join(os.getcwd(), "store", "heatmap", image_name)
        """显示指定路径的图片"""
        if os.path.exists(image_path):
            self.pix = QtGui.QPixmap(image_path)
            self.scaleImage()
            # 如果文件存在，则显示图像
            if os.path.exists(heatmap_path):
                self.ui.label_4.setPixmap(QPixmap(heatmap_path))  # 更新显示的图片
                self.ui.label_4.setScaledContents(True)  # 自动调整大小以适应标签
                self.ui.progressBar.setVisible(False)  # 隐藏进度条
            else:
                # 如果文件不存在，将label_4设置为空白
                self.ui.label_4.clear()  # 清空label_4显示的内容
                self.ui.progressBar.setVisible(False)  # 隐藏进度条
            # 以下根据实际UI组件调整
            # self.ui.image_label.setPixmap(self.pix)
        else:
            print(f"错误：图片文件不存在 {image_path}")

    # 打开图片目录
    # def OpenPictureDir(self):
    #     # 在资源管理器窗口中打开默认路径 返回绝对路径
    #     selectimgFolders, _ = QtWidgets.QFileDialog.getOpenFileNames(None, "选择图像文件", self.defaultImFolder,
    #                                                                  "Image Files (*.png *.jpg *.jpeg)")
    #     # 如果用户取消了文件选择，那么就直接返回
    #     if not selectimgFolders:
    #         print("No images selected")
    #         return
    #
    #
    #     self.defaultImFolder = selectimgFolders[0].rsplit('/', 1)[0]
    #     img_paths=[]
    #     for file in selectimgFolders:
    #         # 确保目标文件夹存在，如果不存在则创建它
    #         current_path = os.getcwd()
    #         target_folder = os.path.join(current_path, 'store', 'larynscope')
    #         os.makedirs(target_folder, exist_ok=True)
    #
    #         if file[-4:] == ".png" or file[-4:] == ".jpg":
    #             # 获取文件的文件名（不带路径）
    #             file_name = os.path.basename(file)
    #
    #             # 构建目标路径
    #             target_path = os.path.join(target_folder, file_name)
    #
    #             # 将文件移动到目标文件夹
    #             shutil.copy(file, target_path)
    #
    #             img_paths.append(target_path)
    #
    #     # 如果有选中路径
    #     if img_paths:
    #         self.img_paths += img_paths
    #         # 获取该路径下面所有的图片名称列表
    #         for file_path in selectimgFolders:
    #             filename = file_path.rsplit('/', 1)[1]
    #             self.imageNameList.append(filename)
    #
    #         self.add_image_items()  # 调用缩略图函数
    #
    #         # 图像显示部分绘制大图
    #         if img_paths:
    #             # 获取第一张图片的路径
    #             # 绘制图片，显示出来
    #             self.pix = QtGui.QPixmap(img_paths[0])
    #             self.scaleImage()
    #             # self.ui.label.setPixmap(self.pix)
    #             # 标记第一张图片的id为0
    #             self.curImgId = 0
    #             self.ui.listWidget.setCurrentRow(self.curImgId)
    #             print("list widget size",self.ui.listWidget.size())
    #     else:
    #         print("No images found")

    # # 上一张图片
    # def ShowBeforeImg(self):
    #     # 如果不存在图片，则跳出
    #     if len(self.imageNameList) == 0:
    #         return
    #     # 当前如果不是第一张图片，才能显示上一张
    #     if self.curImgId > 0:
    #         imPath = os.path.join(self.imgFolder, self.imageNameList[self.curImgId - 1])
    #         self.pix = QtGui.QPixmap(imPath)
    #         self.scaleImage()
    #         self.curImgId = self.curImgId - 1
    #     if self.curImgId < 0:
    #         self.curImgId = 0
    #     self.ui.listWidget.setCurrentRow(self.curImgId)
    #
    # # 下一张图片
    # def ShowNextImg(self):
    #     # 如果不存在图片，则跳出
    #     if len(self.imageNameList) == 0:
    #         return
    #     imgCount = len(self.imageNameList)
    #     # 当前如果不是最后一张图片，才可以显示下一张
    #     if self.curImgId < imgCount - 1:
    #         imPath = os.path.join(self.imgFolder, self.imageNameList[self.curImgId + 1])
    #         print("next1:", self.curImgId)
    #         self.pix = QtGui.QPixmap(imPath)
    #         self.scaleImage()
    #         self.curImgId = self.curImgId + 1
    #         print("next2:", self.curImgId)
    #     self.ui.listWidget.setCurrentRow(self.curImgId)

    # 图片大小随着label大小缩放
    def scaleImage(self):
        # 确保self.pix不是空的
        if self.pix.isNull():
            return
        # 自适应并且宽高比不变(615,447)先暂时锁死
        self.ui.label_oriImg.setPixmap(self.pix
                                       .scaled(615,447, Qt.AspectRatioMode.KeepAspectRatio))
        self.ui.label_oriImg.setAlignment(Qt.AlignmentFlag.AlignCenter)

    def add_image_items(self):  # 缩略图中添加项
        # 清空QListWidget中的所有项
        self.ui.listWidget.clear()

        print(self.img_paths)
        for img_path in self.img_paths:
            if os.path.isfile(img_path):  # 判断图片路径是否存在
                img_name = os.path.basename(img_path)  # 获取图像名
                chinese_part = img_name.split('-')[0]
                item = QListWidgetItem(QIcon(img_path), chinese_part)  # 缩略图图片和名字
                # item.setText(img_name)
                # item.setIcon(QIcon(img_path))
                self.ui.listWidget.addItem(item)

    # 点击缩略图图片显示到label中
    def loadImage(self):
        self.curImgId = self.ui.listWidget.currentIndex().row()
        # print("loadImage:",self.curImgId)
        if self.curImgId in range(len(self.img_paths)):
            self.pix = QPixmap(self.img_paths[self.curImgId])
            image_name = os.path.basename(self.img_paths[self.curImgId])
            heatmap_path = os.path.join(os.getcwd(), "store", "heatmap", image_name)
            # 如果文件存在，则显示图像
            if os.path.exists(heatmap_path):
                self.ui.label_4.setPixmap(QPixmap(heatmap_path))  # 更新显示的图片
                self.ui.label_4.setScaledContents(True)  # 自动调整大小以适应标签
                self.ui.progressBar.setVisible(False)  # 隐藏进度条
            else:
                # 如果文件不存在，将label_4设置为空白
                self.ui.label_4.clear()  # 清空label_4显示的内容
                self.ui.progressBar.setVisible(False)  # 隐藏进度条
            self.scaleImage()







# 修改或添加子界面
class ModOrInsertWindow(QWidget):
    def __init__(self):
        super().__init__()
        self.ui = Ui_modify_and_insert()
        self.ui.setupUi(self)
        self.setWindowFlag(QtCore.Qt.WindowType.FramelessWindowHint)
        self.dragging = False  # 设置拖拽属性默认关闭
        self.offset = None  # 设置偏移属性
        self.setAttribute(QtCore.Qt.WidgetAttribute.WA_TranslucentBackground, True)
        # 创建阴影效果对象
        self.shadow = QtWidgets.QGraphicsDropShadowEffect(self)
        # 设置阴影效果属性
        self.shadow.setOffset(0, 0)  # 偏移量为5像素
        self.shadow.setBlurRadius(10)  # 模糊半径为10像素
        self.shadow.setColor(QtGui.QColor(0, 0, 0, 100))  # 阴影颜色为黑色，透明度为100
        # 将阴影效果应用于窗口
        self.setGraphicsEffect(self.shadow)
        # #绑定事件
        self.ui.pushButton.clicked.connect(self.upload)  # 确认
        self.ui.pushButton_suoxiao.clicked.connect(self.suoxiao)  # 缩小
        # 移动到桌面中间
        self.center()
        self.show()

    def center(self):
        qr = self.frameGeometry()
        cp = QGuiApplication.primaryScreen().availableGeometry().center()
        qr.moveCenter(cp)
        self.move(qr.topLeft())

    def suoxiao(self):
        self.showMinimized()  # 最小化窗口

    # 重写鼠标按下事件
    def mousePressEvent(self, event):
        if event.button() == Qt.MouseButton.LeftButton and self.geometry().contains(self.mapToGlobal(event.pos())):
            self.dis = self.mapToGlobal(event.pos()) - self.pos()
            # print(self.mapToGlobal(event.pos()),event.pos(),self.pos(),self.dis)
            # print("鼠标左键在指定区域内被按下")
            self.dragging = True
            self.setCursor(Qt.CursorShape.ClosedHandCursor)

    # 重写鼠标移动事件
    def mouseMoveEvent(self, event):
        if self.dragging:
            self.move(self.mapToGlobal(event.pos()) - self.dis)

    # 释放鼠标事件
    def mouseReleaseEvent(self, event):
        if event.button() == Qt.MouseButton.LeftButton and self.dragging:
            self.dragging = False
            self.setCursor(Qt.CursorShape.OpenHandCursor)

    # 单选按钮选择
    def upload(self):
        con = Connection(
            host=host_,
            user=username_,
            password=password_,
        )
        self.state = 1  # 状态为1说明输入格式正确，可以进入数据库
        # 检查输入的文本框是否符合格式
        if len(self.ui.lineEdit_5.text()) != 11 or not self.ui.lineEdit_5.text().isdigit():
            self.state = 0
            QMessageBox.warning(self, '错误', '请重新输入电话号码')
        if len(self.ui.lineEdit_1.text()) != 18:
            self.state = 0
            QMessageBox.warning(self, '错误', '请重新输入身份证')
        global insert_record_doctor_id
        global modify_or_add
        if modify_or_add == 2 and self.state == 1:
            if self.ui.lineEdit_1.text() != "" and self.ui.lineEdit_2.text() != "" and self.ui.lineEdit_3.text() != "" and self.ui.lineEdit_4.text() != "" and self.ui.lineEdit_5.text() != "" and self.ui.lineEdit_6.text() != "":
                cur = con.cursor()
                sql = "select * from lary.patients where id=%s"
                new_attr1_value = self.ui.lineEdit_1.text()
                cur.execute(sql, (new_attr1_value,))
                result = cur.fetchone()
                if result:
                    try:
                        with con.cursor() as cur:
                            sql_update = """UPDATE lary.patients
                                            SET name=%s, age=%s, address=%s, phone_num=%s, allergy=%s, sex=%s
                                            WHERE id=%s"""
                            if self.ui.radioButton_man.isChecked():
                                new_value = "男"
                            elif self.ui.radioButton_woman.isChecked():
                                new_value = "女"
                            cur.execute(sql_update, (
                                self.ui.lineEdit_2.text(), self.ui.lineEdit_3.text(), self.ui.lineEdit_4.text(),
                                self.ui.lineEdit_5.text(), self.ui.lineEdit_6.text(), new_value,
                                self.ui.lineEdit_1.text()))
                            con.commit()
                            sql = """INSERT INTO lary.record(patient_id, doctor_id,time)
                                        VALUES (%s, %s,%s)"""
                            # 要插入的新值
                            new_value_1 = self.ui.lineEdit_1.text()
                            new_value_2 = str(insert_record_doctor_id[2][0])
                            now = datetime.now()
                            formatted_now = now.strftime("%Y-%m-%d %H:%M")
                            new_value_3 = formatted_now
                            cur.execute(sql, (new_value_1, new_value_2, new_value_3))
                            con.commit()
                            modify_or_add = 1
                            self.upload()
                            msgBox = QMessageBox(self)
                            msgBox.setWindowTitle('添加成功')
                            msgBox.setText('您的添加已成功完成！')
                            msgBox.setIcon(QMessageBox.Icon.Information)
                            msgBox.setStandardButtons(QMessageBox.StandardButton.Ok)
                            # 连接finished信号到自定义槽函数
                            msgBox.finished.connect(self.onMsgBoxFinished)
                            msgBox.exec()  # 显示消息框并等待用户操作
                    finally:
                        con.close()
                        modify_or_add = 0
                else:
                    try:
                        with con.cursor() as cur:
                            sql = """INSERT INTO lary.patients(id, name, age, address, phone_num, allergy,sex)
                            VALUES (%s, %s, %s, %s, %s, %s,%s)"""
                            # 要插入的新值
                            new_attr1_value = self.ui.lineEdit_1.text()
                            new_attr2_value = self.ui.lineEdit_2.text()
                            new_attr3_value = self.ui.lineEdit_3.text()
                            new_attr4_value = self.ui.lineEdit_4.text()
                            new_attr5_value = self.ui.lineEdit_5.text()
                            new_attr6_value = self.ui.lineEdit_6.text()
                            if self.ui.radioButton_man.isChecked():
                                new_attr7_value = "男"
                            elif self.ui.radioButton_woman.isChecked():
                                new_attr7_value = "女"
                            cur.execute(sql, (new_attr1_value, new_attr2_value, new_attr3_value,
                                              new_attr4_value, new_attr5_value, new_attr6_value, new_attr7_value))
                            sql = """INSERT INTO lary.record(patient_id, doctor_id,time)
                                            VALUES (%s, %s,%s)"""
                            # 要插入的新值
                            new_value_1 = self.ui.lineEdit_1.text()
                            new_value_2 = str(insert_record_doctor_id[2][0])
                            now = datetime.now()
                            formatted_now = now.strftime("%Y-%m-%d %H:%M")
                            new_value_3 = formatted_now
                            cur.execute(sql, (new_value_1, new_value_2, new_value_3))
                            con.commit()
                            msgBox = QMessageBox(self)
                            msgBox.setWindowTitle('添加成功')
                            msgBox.setText('您的添加已成功完成！')
                            msgBox.setIcon(QMessageBox.Icon.Information)
                            msgBox.setStandardButtons(QMessageBox.StandardButton.Ok)
                            # 连接finished信号到自定义槽函数
                            msgBox.finished.connect(self.onMsgBoxFinished)
                            msgBox.exec()  # 显示消息框并等待用户操作
                    finally:
                        con.close()
                        modify_or_add = 0
        elif modify_or_add == 1 and self.state == 1:
            try:
                with con.cursor() as cur:
                    if self.ui.radioButton_man.isChecked():
                        new_value = "男"
                    elif self.ui.radioButton_woman.isChecked():
                        new_value = "女"
                    cur.execute('update lary.patients set name=\'' + self.ui.lineEdit_2.text() +
                                '\',age=\'' + self.ui.lineEdit_3.text() + '\',address=\'' + self.ui.lineEdit_4.text() + '\',phone_num=\'' + self.ui.lineEdit_5.text()
                                + '\',allergy=\'' + self.ui.lineEdit_6.text() + '\',sex=\'' + new_value + '\' where id=\'' + str(
                        modify_id) + '\'')
                    con.commit()
                    # 修改的确认弹窗窗口
                    msgBox = QMessageBox(self)
                    msgBox.setWindowTitle('修改成功')
                    msgBox.setText('您的修改已成功完成！')
                    msgBox.setIcon(QMessageBox.Icon.Information)
                    msgBox.setStandardButtons(QMessageBox.StandardButton.Ok)
                    # 连接finished信号到自定义槽函数
                    msgBox.finished.connect(self.onMsgBoxFinished)
                    msgBox.exec()  # 显示消息框并等待用户操作
            finally:
                con.close()
                modify_or_add = 0

    def onMsgBoxFinished(self):
        self.deleteLater()  # 等待消息框关闭后关闭窗口


# 查看病人的就诊记录
class CheckRecord(QWidget):
    def __init__(self, data):
        super().__init__()
        self.ui = Ui_CheckRecord()
        self.ui.setupUi(self)
        self.setWindowFlag(QtCore.Qt.WindowType.FramelessWindowHint)
        self.dragging = False  # 设置拖拽属性默认关闭
        self.offset = None  # 设置偏移属性
        self.setAttribute(QtCore.Qt.WidgetAttribute.WA_TranslucentBackground, True)
        # 创建阴影效果对象
        self.shadow = QtWidgets.QGraphicsDropShadowEffect(self)
        # 设置阴影效果属性
        self.shadow.setOffset(0, 0)  # 偏移量为5像素
        self.shadow.setBlurRadius(10)  # 模糊半径为10像素
        self.shadow.setColor(QtGui.QColor(0, 0, 0, 100))  # 阴影颜色为黑色，透明度为100
        # 将阴影效果应用于窗口
        self.setGraphicsEffect(self.shadow)
        # 数据
        self.infor = data
        self.patient_id = self.GetData_id(data)
        self.time = self.GetData_time()
        # 病人基本信息设置
        self.information = ("姓名: " + self.infor[1] + "\n" + "性别: " + self.infor[3] + "\n" + "年龄: "
                            + self.infor[2] + "\n"
                            + "身份证号: " + str(self.patient_id).replace("(", "").replace("'", "").replace(",",
                                                                                                            "").replace(
                    ")", ""))
        self.ui.label_2.setText(self.information)
        # 设置tablewidget
        self.ui.tableWidget.setRowCount(len(self.time))
        self.ui.tableWidget.setColumnCount(1)
        self.ui.tableWidget.setEditTriggers(QAbstractItemView.EditTrigger.NoEditTriggers)
        self.ui.tableWidget.setHorizontalHeaderLabels(['就诊时间'])
        self.ui.tableWidget.horizontalHeader().setSectionResizeMode(QHeaderView.ResizeMode.Stretch)
        for i in range(len(self.time)):
            item = QTableWidgetItem(str(self.time[i]))
            item.setTextAlignment(Qt.AlignmentFlag.AlignCenter)  # 设置文本居中
            self.ui.tableWidget.setItem(i, 0, item)
        # 绑定按钮
        self.ui.pushButton_suoxiao.clicked.connect(self.suoxiao)
        self.ui.tableWidget.cellClicked.connect(self.OncellItem)
        self.ui.pushButton.clicked.connect(self.back)
        self.center()
        self.show()

    def GetData_time(self):
        con = Connection(
            host=host_,
            user=username_,
            password=password_,
        )
        cur = con.cursor()
        sql = """select time from lary.record where patient_id =%s"""
        cur.execute(sql, self.patient_id)
        res = cur.fetchall()
        b = []  # 初始化一个空列表来存储处理后的结果
        for row in res:
            time_str = row[0]  # 获取时间字符串
            clean_time_str = time_str.replace("(", "").replace("'", "")
            b.append(clean_time_str)  # 将处理后的时间字符串添加到b列表中
        con.close()
        return b

    def GetData_id(self, data):
        con = Connection(
            host=host_,
            user=username_,
            password=password_,
        )
        cur = con.cursor()
        sql = """select patient_id from lary.record where time =%s"""
        cur.execute(sql, data[0])
        res = cur.fetchone()
        con.close()
        return res

    def OncellItem(self, row, column):
        # 获取就诊时间
        time = self.ui.tableWidget.item(row, column).text()
        con = Connection(
            host=host_,
            user=username_,
            password=password_,
        )
        cur = con.cursor()
        sql = """select result,text from lary.record where time =%s"""
        cur.execute(sql, time)
        res = cur.fetchone()
        con.close()
        # 转向批注那一页
        self.ui.stackedWidget.setCurrentIndex(1)
        if res[0] == None or res[1] == None:
            self.ui.label_result.clear()
            self.ui.label_text.clear()
            msgBox = QMessageBox(self)
            msgBox.setWindowTitle('提醒')
            msgBox.setText('没有该诊断记录')
            msgBox.setIcon(QMessageBox.Icon.Information)
            msgBox.setStandardButtons(QMessageBox.StandardButton.Ok)
            msgBox.exec()  # 显示消息框并等待用户操作
        else:
            self.ui.label_result.setText("诊断结果: " + res[0])
            self.ui.label_text.setText(res[1])

    # 返回上一页
    def back(self):
        self.ui.stackedWidget.setCurrentIndex(0)

    def center(self):
        qr = self.frameGeometry()
        cp = QGuiApplication.primaryScreen().availableGeometry().center()
        qr.moveCenter(cp)
        self.move(qr.topLeft())

    def suoxiao(self):
        self.showMinimized()  # 最小化窗口

    # 重写鼠标按下事件
    def mousePressEvent(self, event):
        if event.button() == Qt.MouseButton.LeftButton and self.geometry().contains(self.mapToGlobal(event.pos())):
            self.dis = self.mapToGlobal(event.pos()) - self.pos()
            self.dragging = True
            self.setCursor(Qt.CursorShape.ClosedHandCursor)

    # 重写鼠标移动事件
    def mouseMoveEvent(self, event):
        if self.dragging:
            self.move(self.mapToGlobal(event.pos()) - self.dis)

    # 释放鼠标事件
    def mouseReleaseEvent(self, event):
        if event.button() == Qt.MouseButton.LeftButton and self.dragging:
            self.dragging = False
            self.setCursor(Qt.CursorShape.OpenHandCursor)


'''MySQL操作,此部分后续可以作为单独的模块'''


def read_user(doctor_id):
    con = Connection(
        host=host_,
        user=username_,
        password=password_,
        database='lary',
    )
    cur = con.cursor()
    cur.execute('SELECT   r.record_id,r.time,p.name,p.age,p.sex FROM record r INNER JOIN patients p ON r.patient_id = p.id WHERE r.doctor_id = %s ORDER BY r.record_id DESC;', doctor_id)

    result = cur.fetchall()
    con.close()
    try:
        colconut = len(result[0])
    except IndexError:
        colconut = 0
    # 返回值:结果,行数,列数
    return result, cur.rowcount, colconut


def read_user_by_id(patient_id):
    global insert_record_doctor_id
    temp = str(insert_record_doctor_id[2][0])
    con = Connection(
        host=host_,
        user=username_,
        password=password_,
    )
    cur = con.cursor()
    cur.execute('SELECT record_id, time, name, age, sex '
                'FROM lary.pat_info '
                'WHERE doctor_id = %s AND id LIKE %s',
                (temp, '%' + patient_id + '%'))
    # cur.execute('select * from temp_wrk_1.test_1')
    result = cur.fetchall()
    con.close()
    try:
        colconut = len(result[0])
    except IndexError:
        colconut = 0
    # 返回值:结果,行数,列数
    return result, cur.rowcount, colconut


def read_user_by_name(patient_name):
    global insert_record_doctor_id
    temp = str(insert_record_doctor_id[2][0])
    con = Connection(
        host=host_,
        user=username_,
        password=password_,
    )
    cur = con.cursor()
    cur.execute('SELECT record_id, time, name, age, sex '
                'FROM lary.pat_info '
                'WHERE doctor_id = %s AND name LIKE %s',
                (temp, '%' + patient_name + '%'))
    # cur.execute('select * from temp_wrk_1.test_1')
    result = cur.fetchall()
    con.close()
    try:
        colconut = len(result[0])
    except IndexError:
        colconut = 0
    # 返回值:结果,行数,列数
    return result, cur.rowcount, colconut


def check_password(username, psw):

    con = Connection(
        host=host_,
        user=username_,
        password=password_,
    )

    cur = con.cursor()
    cur.execute('select id from lary.doctors ' +
                'where username = \'' + username + '\' and password = \'' + psw + '\'')

    result = cur.fetchall()

    return result


def select_patient(patient_id):
    con = Connection(
        host=host_,
        user=username_,
        password=password_,
    )
    cur = con.cursor()
    cur.execute('select * from lary.patients ' +
                'where id = \'' + patient_id + '\'')
    result = cur.fetchall()
    return result


def select_patientid_by_recordid(record_id):
    con = Connection(
        host=host_,
        user=username_,
        password=password_,
    )
    print("def select_patientid_by_recordid(record_id):",record_id)
    with con.cursor() as cur:
        sql = """select patient_id from lary.record where record_id = %s"""
        # 要查找的新值
        new_attr1_value = record_id
        cur.execute(sql, (new_attr1_value))
        res = cur.fetchone()
    con.close()
    return str(res[0])

class ProgressThread(QThread):
    # 自定义信号，用于更新进度条
    update_progress_signal = pyqtSignal(int)

    def run(self):
        total_steps = 50  # 假设分100个步骤
        for step in range(total_steps):
            # 模拟耗时的进度更新
            time.sleep(0.1)  # 每步执行0.1秒，模拟进度
            progress = int((step + 1) / total_steps * 100)
            self.update_progress_signal.emit(progress)


class PredictionThread(QThread):
    finished_signal = pyqtSignal(str)

    def __init__(self, audio_path):
        super().__init__()
        self.audio_path = audio_path

    def run(self):
        # 执行deepPredictVoice预测任务
        # result = deepPredictVoice(self.audio_path)
        # self.finished_signal.emit(result)
        try:
            # 调用预测函数，捕获所有可能异常
            result = deepPredictVoice(self.audio_path)
            print(result)
            self.finished_signal.emit(result)
        except FileNotFoundError as e:
            self.finished_signal.emit(f"错误：文件不存在 - {str(e)}")
        except RuntimeError as e:
            self.finished_signal.emit(f"错误：模型加载/推理失败 - {str(e)}")
        except ValueError as e:
            self.finished_signal.emit(f"错误：音频格式/张量维度不匹配 - {str(e)}")
        except Exception as e:
            self.finished_signal.emit(f"未知错误：{str(e)}")
class preidctVoice(QMainWindow):
    def __init__(self):
        super().__init__()

        self.ui = Ui_predictSubVoice()
        self.ui.setupUi(self)

        global doctor_name
        self.ui.label_doc_name.setText(doctor_name)
        # 初始化pygame音频
        pygame.mixer.init()

        self.current_index = -1  # 当前播放音频的索引
        self.audio_files = []  # 存储音频文件路径

        # 连接控件事件
        self.ui.pushbutton_upVoiceFile.clicked.connect(self.upload_audio)
        self.ui.pushButton_play.clicked.connect(self.play_audio)
        self.ui.pushButton_pause.clicked.connect(self.pause_audio)
        self.ui.pushButton_prev.clicked.connect(self.prev_audio)
        self.ui.pushButton_next.clicked.connect(self.next_audio)
        self.ui.pushButton_replay.clicked.connect(self.replay_audio)
        #诊断按钮
        self.ui.pushButton_predictVoice.clicked.connect(self.predictVoiceShow)

        # 连接双击事件
        self.ui.audioListWidget.itemDoubleClicked.connect(self.play_selected_audio)
        #预测进度条
        self.progress_bar = self.ui.progressBar
        self.progress_bar.setVisible(False)

    def showPatInfo(self, data):
        text = "病人:" + data[1] + "\n" + "年龄:" + data[2] + "\n" + "就诊时间:" + data[0]
        self.ui.label_2.setText(text)
    def predictVoiceShow(self):
        selected_items = self.ui.audioListWidget.selectedItems()
        if selected_items:
            for audio_path in self.audio_files:
                if (audio_path.split("/")[-1] == selected_items[0].text()):
                    # 初始化进度条
                    self.progress_bar.setValue(0)
                    self.progress_bar.setVisible(True)

                    # 创建并启动进度线程
                    self.progress_thread = ProgressThread()
                    self.progress_thread.update_progress_signal.connect(self.update_progress)
                    self.progress_thread.start()

                    # 创建并启动预测线程
                    self.prediction_thread = PredictionThread(audio_path)
                    self.prediction_thread.finished_signal.connect(self.on_prediction_finished)
                    self.prediction_thread.start()
                    break
        else:
            # 使用QMessageBox弹出提示框
            msg = QMessageBox()
            msg.setIcon(QMessageBox.Icon.Information)  # 设置提示框图标
            msg.setText("未选中音频文件")  # 设置提示框的文字
            msg.setWindowTitle("Selection Error")  # 设置标题
            msg.exec()  # 显示提示框

    def update_progress(self, value):
        """更新进度条的值"""
        self.progress_bar.setValue(value)

    def on_prediction_finished(self, result):
        """预测完成后的处理"""
        # self.progress_bar.setValue(100)  # 最后设置为100，表示任务完成
        # self.progress_bar.setVisible(False)  # 隐藏进度条
        # self.ui.label_voiceResult.setText(result)
        # self.ui.label_voiceResult.setAlignment(Qt.AlignmentFlag.AlignCenter)
        # 停止进度线程
        self.progress_bar.setValue(100)  # 最后设置为100，表示任务完成

        # 更新UI显示（错误结果标红，正常结果居中）
        self.progress_bar.setVisible(False)
        if "错误" in result:
            self.ui.label_voiceResult.setText(f"<span style='color:red;'>{result}</span>")
        else:
            self.ui.label_voiceResult.setText(f"{result}")
        self.ui.label_voiceResult.setAlignment(Qt.AlignmentFlag.AlignCenter)

    def upload_audio(self):
        # 打开文件选择对话框，选择多个音频文件
        files, _ = QFileDialog.getOpenFileNames(self, '选择音频文件', '', '音频文件 (*.mp3 *.wav)')
        if files:
            self.audio_files.extend(files)
            # self.ui.audioListWidget.addItems([file.split('/')[-1] for file in files])  # 显示文件名称
            # 将文件名称添加到 QListWidget 中，并设置为选中状态
            for file in files:
                file_name = file.split('/')[-1]
                item = QListWidgetItem(file_name)  # 创建一个 QListWidgetItem
                self.ui.audioListWidget.addItem(item)  # 添加项到 QListWidget
                item.setSelected(True)  # 设置该项为选中状态

    def play_audio(self):
        if self.current_index == -1 and self.audio_files:
            self.current_index = 0
        if self.current_index != -1:
            pygame.mixer.music.load(self.audio_files[self.current_index])
            pygame.mixer.music.play()

    def pause_audio(self):
        if pygame.mixer.music.get_busy():
            pygame.mixer.music.pause()

    def prev_audio(self):
        if self.audio_files:
            self.current_index = (self.current_index - 1) % len(self.audio_files)
            self.play_audio()

    def next_audio(self):
        if self.audio_files:
            self.current_index = (self.current_index + 1) % len(self.audio_files)
            self.play_audio()

    def replay_audio(self):
        if self.current_index != -1:
            pygame.mixer.music.play()

    def play_selected_audio(self, item):
        # 获取被双击的项目对应的索引
        selected_index = self.ui.audioListWidget.row(item)

        # 更新当前音频索引并播放该音频
        if 0 <= selected_index < len(self.audio_files):
            self.current_index = selected_index
            pygame.mixer.music.load(self.audio_files[self.current_index])
            pygame.mixer.music.play()



class preidctVideo(QMainWindow):
    def __init__(self):
        super().__init__()

        self.ui = Ui_predictSubVideo()
        self.ui.setupUi(self)

        # 初始化媒体播放器
        self.mediaPlayer = QMediaPlayer(self)
        self.mediaPlayer.setVideoOutput(self.ui.widget_video)
        print("当前媒体状态:", self.mediaPlayer.mediaStatus())
        # 视频列表
        self.videoList = []
        self.currentIndex = -1  # 当前视频索引
        self.isPaused = True  # 播放状态

        # 定时器更新进度条
        self.timer = QTimer(self)
        self.timer.timeout.connect(self.updateProgress)

        # 连接信号
        self.ui.pushButton_play.clicked.connect(self.togglePlayPause)  # 播放/暂停按钮
        self.ui.pushButton_upfile.clicked.connect(self.uploadFiles)  # 上传文件按钮
        self.ui.Slider.valueChanged.connect(self.changeProgress)  # 进度条拖动

        self.mediaPlayer.errorOccurred.connect(self.handleError)

        # 设置进度条范围
        self.ui.Slider.setRange(0, 100)

    def togglePlayPause(self):
        # 播放/暂停切换
        if self.isPaused:
            # 视频暂停状态
            self.mediaPlayer.play()
            self.ui.pushButton_play.setText('播放')
            self.isPaused = False
        else:
            #播放状态
            self.mediaPlayer.pause()
            self.ui.pushButton_play.setText('暂停')
            self.isPaused = True

    def uploadFiles(self):
        # 上传视频文件
        fileDialog = QFileDialog(self)
        fileDialog.setFileMode(QFileDialog.FileMode.ExistingFiles)
        fileDialog.setNameFilter("视频文件 (*.mp4 *.avi *.mov *.mkv)")
        if fileDialog.exec():
            files = fileDialog.selectedFiles()
            self.videoList.extend(files)

            # 更新视频列表
            for file in files:
                self.ui.listVideo.addItem(file.split('/')[-1])  # 假设 listVideo 是 QListWidget

            # 如果没有播放的视频，则播放第一个
            if self.currentIndex == -1 and len(self.videoList) > 0:
                self.currentIndex = 0
                self.playVideo(self.videoList[self.currentIndex])

    def playVideo(self, videoFile):
        print("当前媒体状态:", self.mediaPlayer.mediaStatus())
            # 设置视频源为 QMediaContent
        self.mediaPlayer.setSource(QUrl.fromLocalFile(videoFile))
        # 播放视频
        self.mediaPlayer.play()
        print("当前媒体状态:", self.mediaPlayer.mediaStatus())
        # 更新UI
        self.ui.pushButton_play.setText('暂停')
        self.isPaused = False

        # 启动定时器更新进度条
        self.timer.start(100)

    def updateProgress(self):
        # 更新进度条
        if self.mediaPlayer.playbackState() == QMediaPlayer.PlaybackState.PlayingState:
            position = self.mediaPlayer.position()
            duration = self.mediaPlayer.duration()
            if duration > 0:
                progress = int((position / duration) * 100)
                self.ui.Slider.setValue(progress)

    def changeProgress(self):
        # 用户拖动进度条时更新视频播放位置
        value = self.ui.Slider.value()
        duration = self.mediaPlayer.duration()
        self.mediaPlayer.setPosition(value * duration / 100)

    def closeEvent(self, event):
        # 释放资源
        self.mediaPlayer.stop()
        self.timer.stop()
        event.accept()

    def showPatInfo(self,data):
        text = "病人:" + data[1] + "\n" + "年龄:" + data[2] + "\n" + "就诊时间:" + data[0]
        self.ui.label_2.setText(text)

    def handleError(self, error):
        print("Error:", error, self.mediaPlayer.errorString())


class AddCase(QMainWindow):
    hasAddCase = pyqtSignal(str)
    def __init__(self):
        super().__init__()
        self.ui = Ui_add_case()  # 加载UI
        self.ui.setupUi(self)

        # 连接按钮的点击事件
        self.ui.pushButton_submit.clicked.connect(self.submit_case)
        self.ui.pushButton_redia.clicked.connect(self.redia)

        # 设置年龄输入框为数字
        self.ui.lineEdit_age.setValidator(QIntValidator(0, 120, self))

        # 设置数据库连接
        self.db = Connection(
            host=host_,  # 数据库主机
            user=username_,  # 数据库用户名
            password=password_,  # 数据库密码
            database="lary"  # 数据库名称
        )
        self.cursor = self.db.cursor()

        # 设置模糊查询
        self.ui.lineEdit_phone.textChanged.connect(self.search_patient)
        self.ui.lineEdit_name.textChanged.connect(self.search_patient)
        self.ui.lineEdit_age.textChanged.connect(self.search_patient)
        self.ui.lineEdit_address.textChanged.connect(self.search_patient)
        self.ui.lineEdit_allergy.textChanged.connect(self.search_patient)
        self.ui.lineEdit_id.textChanged.connect(self.search_patient)
        self.ui.lineEdit_id.textChanged.connect(self.update_age_from_id)

    def submit_case(self):
        # 获取输入框中的值
        phone = self.ui.lineEdit_phone.text()
        name = self.ui.lineEdit_name.text()
        age = self.ui.lineEdit_age.text()
        sex = "女" if self.ui.women.isChecked() else "男"
        allergy = self.ui.lineEdit_allergy.text()
        address = self.ui.lineEdit_address.text()
        id = self.ui.lineEdit_id.text()

        # 输入校验
        if not name or not age or not id:
            QMessageBox.warning(self, "输入错误", "请填写所有必填项!")
            return
        if phone:
            if not phone.isdigit() or len(phone) != 11:  # 假设手机号码为10位数字
                QMessageBox.warning(self, "手机号格式错误", "手机号必须是11位数字!")
                return
        if age:
            if not age.isdigit():
                QMessageBox.warning(self, "年龄格式错误", "年龄必须是数字!")
                return

        # 提交数据到数据库
        try:
            # Assuming doctor_id is a global variable or passed as an argument
            current_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

            # SQL query to insert into the patients table
            query = """
            INSERT INTO patients (id, phone_num, name, age, sex, allergy, address)
            VALUES (%s, %s, %s, %s, %s, %s, %s)
            """
            data = (id, phone, name, int(age), sex, allergy, address)
            self.cursor.execute(query, data)
            self.db.commit()

            # After the patient is successfully added, insert into the record table
            record_query = """
            INSERT INTO record (patient_id, doctor_id, time, result, img_name, imgtag_name, text)
            VALUES (%s, %s, %s, %s, %s, %s, %s)
            """
            # Assuming 'id' is the patient's id and 'doctor_id' is the global variable
            record_data = (id, doctor_id, current_time, None, None, None, None)
            self.cursor.execute(record_query, record_data)
            self.db.commit()

            # Notify the user that the operation was successful
            QMessageBox.information(self, "成功", "信息已提交到数据库")

            # Emit signal and close the window
            self.hasAddCase.emit(str(doctor_id[0][0]))
            self.close()
        except Connection.Error as err:
            QMessageBox.warning(self, "数据库错误", f"提交数据失败: {err}")


    def search_patient(self):
        # 获取模糊查询关键词
        search_term = self.ui.lineEdit_phone.text() or self.ui.lineEdit_name.text() or self.ui.lineEdit_id.text()

        if search_term:
            query = """
            SELECT name, phone_num, id FROM patients
            WHERE name LIKE %s OR phone_num LIKE %s OR id LIKE %s
            """
            like_term = f"%{search_term}%"
            self.cursor.execute(query, (like_term, like_term, like_term))
            results = self.cursor.fetchall()
            # 清空QListWidget并显示查询结果
            self.ui.listWidget_showinfo.clear()

            # 创建表头
            header_layout = QHBoxLayout()
            header_labels = ['姓名', '手机号', '身份证号']
            for label in header_labels:
                header_item = QLabel(label)
                header_item.setFont(QFont("Arial", 12, QFont.Weight.Bold))  # 使用QFont.Weight.Bold代替QFont.Bold
                header_layout.addWidget(header_item)

            # 创建一个 QWidget 作为表头容器
            header_widget = QWidget()
            header_widget.setLayout(header_layout)

            # 将表头添加到 QListWidget
            header_item = QListWidgetItem()
            header_item.setSizeHint(header_widget.sizeHint())  # 设置大小
            header_item.setFlags(header_item.flags() & ~QtCore.Qt.ItemFlag.ItemIsSelectable)  # 设置为不可选中
            self.ui.listWidget_showinfo.addItem(header_item)
            self.ui.listWidget_showinfo.setItemWidget(header_item, header_widget)

            # 添加查询结果
            for row in results:
                row_layout = QHBoxLayout()
                for col in row:
                    col_item = QLabel(str(col))
                    col_item.setFont(QFont("Arial", 10))  # 设置字体大小
                    row_layout.addWidget(col_item)

                # 创建一个 QWidget 作为每一行的容器
                row_widget = QWidget()
                row_widget.setLayout(row_layout)

                # 将每一行数据添加到 QListWidget
                row_item = QListWidgetItem()
                row_item.setSizeHint(row_widget.sizeHint())  # 设置大小
                self.ui.listWidget_showinfo.addItem(row_item)
                self.ui.listWidget_showinfo.setItemWidget(row_item, row_widget)

        else:
            self.ui.listWidget_showinfo.clear()

    def update_age_from_id(self):
        id_number = self.ui.lineEdit_id.text()

        if len(id_number) == 18:  # 判断身份证号是否为18位
            birth_date_str = id_number[6:14]  # 提取出生日期部分（YYYYMMDD）

            try:
                birth_date = datetime.strptime(birth_date_str, "%Y%m%d")  # 转换为日期对象
                age = self.calculate_age(birth_date)
                self.ui.lineEdit_age.setText(str(age))  # 更新年龄
            except ValueError:
                self.ui.lineEdit_age.clear()  # 如果身份证号无效，清空年龄输入框

    def calculate_age(self, birth_date):
        today = datetime.today()
        age = today.year - birth_date.year
        if today.month < birth_date.month or (today.month == birth_date.month and today.day < birth_date.day):
            age -= 1  # 如果生日还没过，年龄减一
        return age

    def redia(self):
        #点击复诊按钮时  应该生成新的record记录
        #内容基于id在患者表中查找
        selected_item = self.ui.listWidget_showinfo.selectedItems()
        patient_id = 0

        # If no items are selected, show a warning message
        if not selected_item:
            msg = QMessageBox(self)
            msg.setIcon(QMessageBox.Icon.Warning)
            msg.setText("Please select a patient first.")
            msg.setWindowTitle("Warning")
            msg.exec()
        else:
            # Get the QWidget corresponding to the selected item
            row_widget = self.ui.listWidget_showinfo.itemWidget(selected_item[0])

            if row_widget:
                # Get the layout of the QWidget
                row_layout = row_widget.layout()

                # Get the third label (which is in the third column, corresponding to patient_id)
                third_label = row_layout.itemAt(2).widget()  # Index starts from 0, so third item is at index 2

                # Get the text content of that label, which should be the patient_id
                patient_id = third_label.text()

            # Now we have the patient_id, let's query the patients table for this patient
            try:
                # Connect to the MySQL database
                con = Connection(
                    host=host_,  # e.g., 'localhost'
                    user=username_,
                    password=password_,
                    database='lary'
                )
                cursor = con.cursor()

                # Query the patients table to fetch details for the selected patient
                cursor.execute("SELECT * FROM patients WHERE id = %s", (patient_id,))
                patient = cursor.fetchone()

                if not patient:
                    # If the patient does not exist, show an error message
                    msg = QMessageBox(self)
                    msg.setIcon(QMessageBox.Icon.Critical)
                    msg.setText(f"No patient found with ID {patient_id}.")
                    msg.setWindowTitle("Error")
                    msg.exec()
                else:
                    # If the patient exists, create a new record in the record table
                    # Get the current time for the record creation
                    current_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

                    # Insert a new record into the record table
                    insert_query = """
                        INSERT INTO record (patient_id, doctor_id, time)
                        VALUES (%s, %s, %s)
                    """

                    # Define placeholders for the values in the record
                    result = "Re-examination result"  # Adjust this based on what you want to store
                    img_name = None  # You can adjust if you have image names to store
                    imgtag_name = None  # You can adjust if you have image tag names
                    text = "Patient needs further review."  # You can modify this based on your requirements
                    global doctor_id
                    # Execute the insert query
                    cursor.execute(insert_query,
                                   (patient_id, doctor_id, current_time))

                    # Commit the changes to the database
                    con.commit()

                    self.hasAddCase.emit(str(doctor_id[0][0]))
                    self.close()

            except Connection.Error as e:
                # If there's an error with the database connection or query, show an error message
                msg = QMessageBox(self)
                msg.setIcon(QMessageBox.Icon.Critical)
                msg.setText(f"Database error: {e}")
                msg.setWindowTitle("Error")
                msg.exec()
            finally:
                # Close the database connection
                con.close()



if __name__ == '__main__':
    app = QApplication(sys.argv)
    loginWindow = LoginWindow()
    sys.exit(app.exec())
