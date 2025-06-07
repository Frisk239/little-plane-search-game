package com.frame;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class RankingBoardFrame extends JFrame {
    private JTable rankingTable;
    private JButton refreshButton;
    private RankingDB rankingDB;

    public RankingBoardFrame() {
        setTitle("游戏排行榜");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        rankingDB = new RankingDB();
        initComponents();
        loadRankingData();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // 表格模型
        DefaultTableModel model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[]{"排名", "玩家名称", "用时(秒)", "游戏时间"});
        rankingTable = new JTable(model);
        rankingTable.setRowHeight(30);
        rankingTable.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        
        JScrollPane scrollPane = new JScrollPane(rankingTable);
        
        // 刷新按钮
        refreshButton = new JButton("刷新排行榜");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadRankingData();
            }
        });
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refreshButton);
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }

    private void loadRankingData() {
        DefaultTableModel model = (DefaultTableModel) rankingTable.getModel();
        model.setRowCount(0); // 清空表格
        
        List<RankingDB.RankingEntry> rankings = rankingDB.getTopScores(10);
        
        int rank = 1;
        for (RankingDB.RankingEntry entry : rankings) {
            model.addRow(new Object[]{
                rank++,
                entry.playerName,
                entry.score,
                entry.gameTime.toString()
            });
        }
    }

    @Override
    public void dispose() {
        rankingDB.close();
        super.dispose();
    }
}
